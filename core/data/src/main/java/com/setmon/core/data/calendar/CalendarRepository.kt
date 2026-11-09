package com.setmon.core.data.calendar

import com.setmon.core.model.CalendarDaySummary
import java.time.YearMonth

/** 달력 창구. 화면(feature:calendar)은 이 인터페이스만 본다. */
interface CalendarRepository {
    suspend fun getMonth(userId: Long, month: YearMonth): CalendarMonthResult
}

sealed interface CalendarMonthResult {
    data class Success(val days: List<CalendarDaySummary>) : CalendarMonthResult
    data class Failure(val error: CalendarError) : CalendarMonthResult
}

enum class CalendarError { NETWORK, UNKNOWN }
