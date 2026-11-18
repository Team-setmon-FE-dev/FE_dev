package com.setmon.core.network.calendar

import com.setmon.core.model.CalendarDaySummary
import com.setmon.core.model.DayClip
import com.setmon.core.model.DayDetail
import com.setmon.core.model.DayVlog
import com.setmon.core.model.DayVlogStatus
import com.setmon.core.network.ApiResult
import com.setmon.core.network.ServerClient
import com.setmon.core.network.apiCall
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRemoteDataSource @Inject constructor(
    private val serverClient: ServerClient,
) {
    private val api: CalendarApi by lazy { serverClient.retrofit.create(CalendarApi::class.java) }

    suspend fun getMonth(userId: Long, month: YearMonth): ApiResult<List<CalendarDaySummary>> = apiCall {
        api.getMonth(userId, month.toString()).days.map { day ->
            CalendarDaySummary(
                date = LocalDate.parse(day.date),
                thumbnailUrl = day.thumbnailUrl,
                clipCount = day.clipCount,
            )
        }
    }

    suspend fun getDay(userId: Long, date: LocalDate): ApiResult<DayDetail> = apiCall {
        val day = api.getDay(userId, date.toString())
        DayDetail(
            userId = day.userId,
            date = LocalDate.parse(day.date),
            notifyStartHour = day.notifyStartHour,
            notifyEndHour = day.notifyEndHour,
            vlog = day.vlog?.let { vlog ->
                DayVlog(
                    status = DayVlogStatus.entries.firstOrNull { it.name == vlog.status } ?: DayVlogStatus.NONE,
                    stale = vlog.stale,
                    videoUrl = vlog.videoUrl,
                    thumbnailUrl = vlog.thumbnailUrl,
                    failReason = vlog.failReason,
                )
            } ?: DayVlog(DayVlogStatus.NONE, stale = false, videoUrl = null, thumbnailUrl = null, failReason = null),
            clips = day.clips
                .map { DayClip(it.id, it.slotHour, it.durationMs, it.videoUrl, it.thumbnailUrl, isCover = it.cover) }
                .sortedBy { it.slotHour },
        )
    }
}
