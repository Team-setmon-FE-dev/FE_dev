package com.setmon.core.data.calendar

import com.setmon.core.model.CalendarDaySummary
import com.setmon.core.model.todayInKorea
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 서버·업로드 없이 달력 칸을 돌려보기 위한 가짜 구현.
 * 썸네일은 debug 빌드에만 들어 있는 샘플 사진이다.
 */
@Singleton
class FakeCalendarRepository @Inject constructor() : CalendarRepository {
    override suspend fun getMonth(userId: Long, month: YearMonth): CalendarMonthResult {
        val today = todayInKorea()
        val pastDays = (1..month.lengthOfMonth())
            .map(month::atDay)
            .filter { it.isBefore(today) && it.dayOfMonth % 3 == 1 }
            .map { CalendarDaySummary(it, FAKE_THUMBNAIL, clipCount = 1) }
        val todaySummary = if (YearMonth.from(today) == month) {
            listOf(CalendarDaySummary(today, FAKE_THUMBNAIL, clipCount = 3))
        } else {
            emptyList()
        }
        return CalendarMonthResult.Success(pastDays + todaySummary)
    }

    private companion object {
        const val FAKE_THUMBNAIL = "android.resource://com.setmon.app/drawable/sample_clip_thumbnail"
    }
}
