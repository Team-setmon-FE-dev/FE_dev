package com.setmon.feature.calendar

import com.setmon.core.designsystem.component.DayCellState
import com.setmon.core.model.CalendarDaySummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class CalendarGridTest {
    private val november = YearMonth.of(2026, 11) // 11월 1일이 일요일
    private val today = LocalDate.of(2026, 11, 10)

    @Test
    fun `일요일로 시작하는 달은 앞 빈칸이 없다`() {
        val weeks = buildCalendarWeeks(november, today, emptyList())

        assertEquals(LocalDate.of(2026, 11, 1), weeks.first().first().date)
        assertEquals(5, weeks.size)
    }

    @Test
    fun `첫날의 요일만큼 앞을 비우고 모든 주는 7칸이다`() {
        val weeks = buildCalendarWeeks(YearMonth.of(2026, 12), today, emptyList()) // 12월 1일은 화요일

        assertNull(weeks[0][0].date)
        assertNull(weeks[0][1].date)
        assertEquals(LocalDate.of(2026, 12, 1), weeks[0][2].date)
        assertTrue(weeks.all { it.size == 7 })
    }

    @Test
    fun `날짜별 상태를 정한다`() {
        val days = listOf(
            CalendarDaySummary(LocalDate.of(2026, 11, 3), "thumb", 1),
            CalendarDaySummary(today, "today-thumb", 3),
        )

        val cells = buildCalendarWeeks(november, today, days).flatten().associateBy { it.date }

        assertEquals(DayCellState.Thumbnail, cells[LocalDate.of(2026, 11, 3)]?.state)
        assertEquals(DayCellState.Empty, cells[LocalDate.of(2026, 11, 4)]?.state)
        assertEquals(DayCellState.Today, cells[today]?.state)
        assertEquals(3, cells[today]?.clipCount)
        assertEquals(DayCellState.Future, cells[LocalDate.of(2026, 11, 11)]?.state)
    }
}
