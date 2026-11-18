package com.setmon.core.data.cover

import android.content.Context
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 달력 칸 대표 클립(썸네일)을 **이 기기에만** 저장한다 (11-18 사용자 결정: 서버 변경 없이).
 * - 어떤 클립을 골랐는지: SharedPreferences  "{userId}_{yyyy-MM-dd}" → clipId
 * - 사진: 서버 주소는 1시간이면 만료되므로 고를 때 썸네일을 내려받아 files/covers 에 둔다
 *   파일 이름에 clipId 를 넣어 바꿀 때마다 새 이름 → 사진 캐시가 옛 사진을 보여 주지 않는다
 * 친구 화면·다른 기기에서는 기본 썸네일(브이로그 → 첫 클립)로 보인다.
 */
@Singleton
class DayCoverStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val dir: File get() = File(context.filesDir, DIR).apply { mkdirs() }

    fun coverClipId(userId: Long, date: LocalDate): Long? =
        prefs.getLong(key(userId, date), NONE).takeIf { it != NONE }

    /** 고른 클립의 썸네일을 내려받아 대표로 저장한다. 실패하면 false (이전 대표는 그대로) */
    suspend fun setCover(userId: Long, date: LocalDate, clipId: Long, thumbnailUrl: String?): Boolean =
        withContext(Dispatchers.IO) {
            if (thumbnailUrl == null) return@withContext false
            val target = File(dir, fileName(userId, date, clipId))
            try {
                URL(thumbnailUrl).openStream().use { input -> target.outputStream().use { input.copyTo(it) } }
            } catch (e: Exception) {
                Log.w(TAG, "대표 썸네일 내려받기 실패", e)
                target.delete()
                return@withContext false
            }
            deleteFiles(userId, date, except = target)
            prefs.edit().putLong(key(userId, date), clipId).apply()
            true
        }

    /** 대표 해제 → 기본 썸네일로 */
    suspend fun clearCover(userId: Long, date: LocalDate) = withContext(Dispatchers.IO) {
        deleteFiles(userId, date, except = null)
        prefs.edit().remove(key(userId, date)).apply()
    }

    /** 달력에 쓸 대표 사진들: 날짜 → 기기 안 사진 주소(file://) */
    suspend fun coverImages(userId: Long): Map<LocalDate, String> = withContext(Dispatchers.IO) {
        val prefix = "${userId}_"
        prefs.all.mapNotNull { (key, value) ->
            if (!key.startsWith(prefix) || value !is Long) return@mapNotNull null
            val date = runCatching { LocalDate.parse(key.removePrefix(prefix)) }.getOrNull() ?: return@mapNotNull null
            val file = File(dir, fileName(userId, date, value))
            if (file.exists()) date to Uri.fromFile(file).toString() else null
        }.toMap()
    }

    private fun deleteFiles(userId: Long, date: LocalDate, except: File?) {
        val prefix = "${userId}_${date}_"
        dir.listFiles()?.filter { it.name.startsWith(prefix) && it != except }?.forEach { it.delete() }
    }

    private fun key(userId: Long, date: LocalDate) = "${userId}_$date"

    private fun fileName(userId: Long, date: LocalDate, clipId: Long) = "${userId}_${date}_$clipId.jpg"

    private companion object {
        const val TAG = "SetmonCover"
        const val PREFS = "day_covers"
        const val DIR = "covers"
        const val NONE = -1L
    }
}
