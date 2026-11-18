package com.setmon.core.model

import java.time.LocalDate

/** 하루 브이로그 상태 (서버 daily_vlogs.status). NONE = 아직 만든 적 없음 */
enum class DayVlogStatus {
    NONE, PENDING, PROCESSING, DONE, FAILED;

    val isGenerating: Boolean get() = this == PENDING || this == PROCESSING
}

data class DayVlog(
    val status: DayVlogStatus,
    /** 만든 뒤 클립이 바뀌어 다시 만들어야 함 */
    val stale: Boolean,
    /** DONE 일 때만 (1시간 유효) */
    val videoUrl: String?,
    val thumbnailUrl: String?,
    /** FAILED 일 때만 */
    val failReason: String?,
)

data class DayClip(
    val id: Long,
    /** 몇 시 칸에 찍었는지 (KST 0~23) */
    val slotHour: Int,
    val durationMs: Int,
    /** 1시간 유효 */
    val videoUrl: String,
    val thumbnailUrl: String?,
    /** 사용자가 고른 그날 달력 칸 대표 (하루 최대 1개) */
    val isCover: Boolean = false,
)

/** S-05 하루 상세 (GET /api/users/{userId}/days/{date}) */
data class DayDetail(
    val userId: Long,
    val date: LocalDate,
    /** 타임라인 칸 범위 = 달력 주인의 알림 시간대 (KST, 둘 다 포함) */
    val notifyStartHour: Int,
    val notifyEndHour: Int,
    val vlog: DayVlog,
    /** slotHour 오름차순 */
    val clips: List<DayClip>,
)
