package com.setmon.core.data.media

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.setmon.core.network.di.AuthedOkHttp
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * MediaStore 로 `Movies/Setmon` 에 저장한다.
 * - Android 10+ : 권한 없이 MediaStore 에 바로 쓴다 (IS_PENDING 으로 다 쓰기 전엔 갤러리에 안 보이게)
 * - Android 8~9 : 공용 Movies 폴더에 파일로 쓰고 미디어 스캔. WRITE_EXTERNAL_STORAGE 권한 필요
 *
 * OkHttp 는 core:network 의 것을 쓴다. S3 주소에는 토큰이 붙지 않는다(AuthInterceptor 가 주소를 확인).
 */
internal class MediaStoreVlogSaver @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:AuthedOkHttp private val okHttpClient: OkHttpClient,
) : VlogSaver {

    override suspend fun saveToGallery(url: String, fileName: String): VlogSaveResult =
        withContext(Dispatchers.IO) {
            val displayName = sanitize(fileName) + ".mp4"
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !hasLegacyWritePermission()) {
                return@withContext VlogSaveResult.Failure(VlogSaveError.PERMISSION_REQUIRED)
            }
            try {
                okHttpClient.newCall(Request.Builder().url(url).get().build()).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.w(TAG, "download failed code=${response.code}")
                        // S3 presigned URL 은 만료되면 403 을 준다
                        val error = if (response.code == 403 || response.code == 400) {
                            VlogSaveError.URL_EXPIRED
                        } else {
                            VlogSaveError.NETWORK
                        }
                        return@withContext VlogSaveResult.Failure(error)
                    }
                    val body = response.body ?: return@withContext VlogSaveResult.Failure(VlogSaveError.NETWORK)
                    val uri = body.byteStream().use { input ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            writeWithMediaStore(input, displayName)
                        } else {
                            writeLegacy(input, displayName)
                        }
                    }
                    VlogSaveResult.Success(uri)
                }
            } catch (e: StorageException) {
                Log.w(TAG, "save failed", e)
                VlogSaveResult.Failure(VlogSaveError.STORAGE)
            } catch (e: IOException) {
                // 다운로드 중 끊김. 저장 중 디스크 오류도 IOException 이지만 StorageException 으로 먼저 걸러진다
                Log.w(TAG, "download failed", e)
                VlogSaveResult.Failure(VlogSaveError.NETWORK)
            } catch (e: SecurityException) {
                // Android 8~9 에서 저장 도중 권한이 취소된 경우
                Log.w(TAG, "no permission", e)
                VlogSaveResult.Failure(VlogSaveError.PERMISSION_REQUIRED)
            } catch (e: IllegalArgumentException) {
                // URL 형식이 잘못됨
                Log.w(TAG, "bad url", e)
                VlogSaveResult.Failure(VlogSaveError.URL_EXPIRED)
            }
        }

    override fun shareIntent(uri: Uri, title: String): Intent {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(send, title)
    }

    /** Android 10+ */
    @androidx.annotation.RequiresApi(Build.VERSION_CODES.Q)
    private fun writeWithMediaStore(input: InputStream, displayName: String): Uri {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Video.Media.MIME_TYPE, MIME_TYPE)
            put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/$ALBUM")
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }
        val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = resolver.insert(collection, values) ?: throw StorageException("MediaStore insert 실패")
        try {
            val out = resolver.openOutputStream(uri) ?: throw StorageException("출력 스트림 없음")
            out.use { copy(input, it) }
            values.clear()
            values.put(MediaStore.Video.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            return uri
        } catch (e: Exception) {
            // 반쯤 쓴 파일이 갤러리에 남지 않게 지운다
            resolver.delete(uri, null, null)
            throw e
        }
    }

    /** Android 8~9 */
    @Suppress("DEPRECATION")
    private suspend fun writeLegacy(input: InputStream, displayName: String): Uri {
        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), ALBUM)
        if (!dir.exists() && !dir.mkdirs()) throw StorageException("폴더를 만들 수 없음: $dir")
        val file = uniqueFile(dir, displayName)
        try {
            file.outputStream().use { copy(input, it) }
        } catch (e: Exception) {
            file.delete()
            throw e
        }
        // 갤러리에 보이게 스캔하고, 공유에 쓸 content:// uri 를 받는다
        return suspendCancellableCoroutine { cont ->
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf(MIME_TYPE)) { _, uri ->
                if (uri != null) cont.resume(uri) else cont.resume(Uri.fromFile(file))
            }
        }
    }

    /** 다운로드 끊김(IOException)과 저장 실패를 구분하려고 쓰기 오류만 StorageException 으로 바꾼다 */
    private fun copy(input: InputStream, output: java.io.OutputStream) {
        val buffer = ByteArray(64 * 1024)
        while (true) {
            val read = input.read(buffer) // 여기서 나는 IOException 은 네트워크
            if (read < 0) break
            try {
                output.write(buffer, 0, read)
            } catch (e: IOException) {
                throw StorageException("저장 실패: ${e.message}", e)
            }
        }
        try {
            output.flush()
        } catch (e: IOException) {
            throw StorageException("저장 실패: ${e.message}", e)
        }
    }

    private fun uniqueFile(dir: File, displayName: String): File {
        val base = displayName.removeSuffix(".mp4")
        var file = File(dir, displayName)
        var n = 1
        while (file.exists()) {
            file = File(dir, "$base ($n).mp4")
            n++
        }
        return file
    }

    private fun hasLegacyWritePermission(): Boolean =
        context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun sanitize(name: String): String =
        name.replace(Regex("[^0-9A-Za-z가-힣._-]"), "_").ifBlank { "setmon_vlog" }

    private class StorageException(message: String, cause: Throwable? = null) : IOException(message, cause)

    private companion object {
        const val TAG = "SetmonVlogSaver"
        const val MIME_TYPE = "video/mp4"
        const val ALBUM = "Setmon"
    }
}
