package com.setmon.feature.capture

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.setmon.core.data.media.VlogSaveError
import com.setmon.core.data.media.VlogSaveResult
import com.setmon.core.data.media.VlogSaver
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 갤러리 저장·공유 모듈(core:data VlogSaver)을 실제 기기·에뮬레이터에서 확인한다 (3주차, B).
 *
 * 영상 URL 은 실행할 때 넘긴다 (S3 presigned GET):
 * ```
 * adb shell am instrument -w \
 *   -e validUrl '<1시간짜리 URL>' -e expiredUrl '<만료된 URL>' \
 *   com.setmon.feature.capture.test/androidx.test.runner.AndroidJUnitRunner
 * ```
 * URL 이 없으면 해당 테스트는 건너뛴다.
 */
@RunWith(AndroidJUnit4::class)
class VlogSaverDeviceTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val args = InstrumentationRegistry.getArguments()

    /** 구현 클래스는 core:data 안에서만 보이므로(internal) 테스트에서는 이름으로 만든다 */
    private val saver: VlogSaver = Class.forName("com.setmon.core.data.media.MediaStoreVlogSaver")
        .getDeclaredConstructor(Context::class.java, OkHttpClient::class.java)
        .newInstance(context, OkHttpClient()) as VlogSaver

    @Test
    fun 정상_URL은_Movies_Setmon에_저장되고_공유_Intent가_만들어진다() = runBlocking {
        val url = args.getString("validUrl")
        assumeTrue("validUrl 인자가 없어 건너뜀", !url.isNullOrBlank())
        assumeTrue("Android 10 이상에서 확인", Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)

        val result = saver.saveToGallery(url!!, "setmon_device_test")

        assertTrue("저장 실패: $result", result is VlogSaveResult.Success)
        val uri = (result as VlogSaveResult.Success).uri
        try {
            val projection = arrayOf(
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.RELATIVE_PATH,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.IS_PENDING,
            )
            context.contentResolver.query(uri, projection, null, null, null)!!.use { c ->
                assertTrue("저장된 항목을 찾을 수 없음", c.moveToFirst())
                val name = c.getString(0)
                val path = c.getString(1)
                val size = c.getLong(2)
                println("SETMON_TEST saved name=$name path=$path size=$size uri=$uri")
                assertTrue(name, name.startsWith("setmon_device_test") && name.endsWith(".mp4"))
                assertEquals("Movies/Setmon/", path)
                assertTrue("파일 크기 0", size > 0)
                assertEquals("video/mp4", c.getString(3))
                assertEquals("저장이 끝났는데 아직 숨김 상태", 0, c.getInt(4))
            }

            val chooser = saver.shareIntent(uri)
            assertEquals(Intent.ACTION_CHOOSER, chooser.action)
            @Suppress("DEPRECATION")
            val send = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
            assertEquals(Intent.ACTION_SEND, send.action)
            assertEquals("video/mp4", send.type)
            @Suppress("DEPRECATION")
            assertEquals(uri, send.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
            assertTrue(send.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        } finally {
            // 테스트 파일은 갤러리에 남기지 않는다
            context.contentResolver.delete(uri, null, null)
        }
    }

    @Test
    fun 만료된_URL은_URL_EXPIRED() = runBlocking {
        val url = args.getString("expiredUrl")
        assumeTrue("expiredUrl 인자가 없어 건너뜀", !url.isNullOrBlank())

        val result = saver.saveToGallery(url!!, "setmon_expired_test")

        assertEquals(VlogSaveResult.Failure(VlogSaveError.URL_EXPIRED), result)
        assertNoLeftover("setmon_expired_test")
    }

    @Test
    fun 접속할_수_없는_주소는_NETWORK() = runBlocking {
        val result = saver.saveToGallery("https://setmon-no-such-host.invalid/v.mp4", "setmon_network_test")

        assertEquals(VlogSaveResult.Failure(VlogSaveError.NETWORK), result)
        assertNoLeftover("setmon_network_test")
    }

    /** 실패했을 때 반쯤 쓴 파일이 갤러리에 남지 않아야 한다 */
    private fun assertNoLeftover(prefix: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        context.contentResolver.query(
            collection,
            arrayOf(MediaStore.Video.Media._ID),
            "${MediaStore.Video.Media.DISPLAY_NAME} LIKE ?",
            arrayOf("$prefix%"),
            null,
        )!!.use { c ->
            val left = mutableListOf<Uri>()
            while (c.moveToNext()) left += ContentUris.withAppendedId(collection, c.getLong(0))
            left.forEach { context.contentResolver.delete(it, null, null) }
            assertEquals("실패한 저장이 갤러리에 남음", 0, left.size)
        }
    }
}
