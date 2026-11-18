package com.setmon.feature.calendar

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

internal sealed interface CalendarImageResult {
    data class Success(val uri: Uri) : CalendarImageResult

    /** Android 8~9 에서 저장 권한이 없음 → 화면이 권한을 요청한 뒤 다시 */
    data object PermissionRequired : CalendarImageResult
    data object Failure : CalendarImageResult
}

/**
 * 한 달 달력 그림을 갤러리 Pictures/Setmon 에 PNG 로 저장한다 (공유는 브이로그만).
 * (영상 저장은 B 의 VlogSaver, 이건 A 의 달력 사진 저장)
 * - Android 10+ : 권한 없이 MediaStore 에 쓴다
 * - Android 8~9 : WRITE_EXTERNAL_STORAGE (core:data 매니페스트에 maxSdk 28 로 선언돼 있음)
 */
@Singleton
internal class CalendarImageSaver @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    /**
     * @param image 화면에서 뜬 달력 그림 (배경이 투명할 수 있다)
     * @param backgroundColor 투명한 곳을 채울 색 (ARGB)
     */
    suspend fun save(image: Bitmap, backgroundColor: Int, fileName: String): CalendarImageResult =
        withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                return@withContext CalendarImageResult.PermissionRequired
            }
            try {
                val flattened = flatten(image, backgroundColor)
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    writeWithMediaStore(flattened, "$fileName.png")
                } else {
                    writeLegacy(flattened, "$fileName.png")
                }
                CalendarImageResult.Success(uri)
            } catch (e: Exception) {
                android.util.Log.w("SetmonCalendar", "달력 사진 저장 실패", e)
                CalendarImageResult.Failure
            }
        }

    /** 하드웨어 비트맵일 수 있어 일반 비트맵으로 옮기면서 배경색을 깐다 */
    private fun flatten(image: Bitmap, backgroundColor: Int): Bitmap {
        val source = if (image.config == Bitmap.Config.HARDWARE) image.copy(Bitmap.Config.ARGB_8888, false) else image
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        Canvas(result).apply {
            drawColor(backgroundColor)
            drawBitmap(source, 0f, 0f, null)
        }
        return result
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.Q)
    private fun writeWithMediaStore(bitmap: Bitmap, displayName: String): Uri {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, MIME_TYPE)
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$FOLDER")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("MediaStore insert 실패")
        try {
            resolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                ?: error("파일을 열지 못함")
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            return uri
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            throw e
        }
    }

    /**
     * Android 8~9: 공용 Pictures 폴더에 쓰고 갤러리가 알도록 스캔.
     * 스캔이 돌려주는 content:// 를 결과로 쓴다
     */
    @Suppress("DEPRECATION")
    private suspend fun writeLegacy(bitmap: Bitmap, displayName: String): Uri {
        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), FOLDER)
        if (!dir.exists() && !dir.mkdirs()) error("폴더를 만들 수 없음: $dir")
        val file = File(dir, displayName)
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return suspendCancellableCoroutine { cont ->
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf(MIME_TYPE)) { _, uri ->
                if (uri != null) cont.resume(uri) else cont.resume(Uri.fromFile(file))
            }
        }
    }

    private companion object {
        const val MIME_TYPE = "image/png"
        const val FOLDER = "Setmon"
    }
}
