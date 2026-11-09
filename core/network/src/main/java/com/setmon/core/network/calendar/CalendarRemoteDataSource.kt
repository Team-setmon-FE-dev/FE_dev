package com.setmon.core.network.calendar

import com.setmon.core.model.CalendarDaySummary
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
}
