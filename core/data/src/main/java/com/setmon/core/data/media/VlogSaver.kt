package com.setmon.core.data.media

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * 브이로그 갤러리 저장·공유 (3주차, B → A 의 S-05 에서 사용).
 *
 * 사용 예 (ViewModel 에서 주입받아):
 * ```
 * when (val r = vlogSaver.saveToGallery(day.vlog.videoUrl, fileName = "setmon_${day.date}")) {
 *     is VlogSaveResult.Success -> context.startActivity(vlogSaver.shareIntent(r.uri)) // 공유까지 할 때
 *     is VlogSaveResult.Failure -> when (r.error) {
 *         VlogSaveError.URL_EXPIRED -> // 하루 상세를 다시 조회해 새 URL 로 재시도
 *         VlogSaveError.PERMISSION_REQUIRED -> // Android 8~9: 저장 권한 요청 후 재시도
 *         else -> // 안내 문구
 *     }
 * }
 * ```
 */
interface VlogSaver {

    /**
     * 영상 URL(S3 presigned GET)을 내려받아 갤러리 `Movies/Setmon` 에 저장한다.
     *
     * @param url 하루 상세 조회의 `vlog.videoUrl` (1시간 유효)
     * @param fileName 확장자 없는 파일 이름. 예: "setmon_2026-11-17". 같은 이름이 있으면 시스템이 번호를 붙인다
     */
    suspend fun saveToGallery(url: String, fileName: String): VlogSaveResult

    /**
     * 저장한 영상을 안드로이드 공유 시트로 보내는 Intent. `startActivity(...)` 로 띄운다.
     * 갤러리에 저장하지 않고 공유만 하려면 먼저 [saveToGallery] 로 저장한 뒤 그 uri 를 쓴다.
     */
    fun shareIntent(uri: Uri, title: String = "브이로그 공유"): Intent

    /** [shareIntent] 를 바로 띄운다. Activity 가 아닌 Context 여도 동작한다 */
    fun share(context: Context, uri: Uri, title: String = "브이로그 공유") {
        val intent = shareIntent(uri, title)
        if (context !is android.app.Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

sealed interface VlogSaveResult {
    /** @param uri 갤러리에 저장된 영상 (content://...). 공유에 그대로 쓴다 */
    data class Success(val uri: Uri) : VlogSaveResult
    data class Failure(val error: VlogSaveError) : VlogSaveResult
}

enum class VlogSaveError {
    /** 인터넷 끊김·서버 응답 없음 */
    NETWORK,

    /** URL 이 만료됨(1시간). 하루 상세를 다시 조회해 새 URL 로 시도 */
    URL_EXPIRED,

    /** Android 8~9 에서 저장 권한(WRITE_EXTERNAL_STORAGE)이 없음 */
    PERMISSION_REQUIRED,

    /** 기기 저장 공간 부족 등 저장 실패 */
    STORAGE,
}
