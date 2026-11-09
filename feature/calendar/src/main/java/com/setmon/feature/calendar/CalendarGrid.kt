package com.setmon.feature.calendar

import com.setmon.core.designsystem.component.DayCellState
import com.setmon.core.model.CalendarDaySummary
import java.time.LocalDate
import java.time.YearMonth

internal data class CalendarCell(
    val date: LocalDate?,
    val state: DayCellState,
    val thumbnailUrl: String? = null,
    val clipCount: Int = 0,
)

private const val DAYS_IN_WEEK = 7
private val BLANK_CELL = CalendarCell(date = null, state = DayCellState.Blank)

/** 일요일 시작 달력. 앞뒤의 다른 달 자리는 빈칸으로 채워서 한 주는 항상 7칸이다 */
internal fun buildCalendarWeeks(
    month: YearMonth,
    today: LocalDate,
    days: List<CalendarDaySummary>,
): List<List<CalendarCell>> {
    val summaries = days.associateBy { it.date }
    val leadingBlanks = month.atDay(1).dayOfWeek.value % DAYS_IN_WEEK // 일요일(7) → 0
    val cells = List(leadingBlanks) { BLANK_CELL } +
        (1..month.lengthOfMonth()).map { dayOfMonth ->
            val date = month.atDay(dayOfMonth)
            val summary = summaries[date]
            CalendarCell(
                date = date,
                state = when {
                    date == today -> DayCellState.Today
                    date.isAfter(today) -> DayCellState.Future
                    summary?.thumbnailUrl != null -> DayCellState.Thumbnail
                    else -> DayCellState.Empty
                },
                thumbnailUrl = summary?.thumbnailUrl,
                clipCount = summary?.clipCount ?: 0,
            )
        }
    return cells.chunked(DAYS_IN_WEEK) { week -> week + List(DAYS_IN_WEEK - week.size) { BLANK_CELL } }
}
