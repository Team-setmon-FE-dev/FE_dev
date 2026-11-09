package com.setmon.core.model

import java.time.LocalDate

/** 달력 칸 하나의 내용. 기록이 있는 날만 서버가 내려준다 */
data class CalendarDaySummary(
    val date: LocalDate,
    /** 브이로그 썸네일, 없으면 첫 클립 썸네일 (1시간 유효) */
    val thumbnailUrl: String?,
    val clipCount: Int,
)
