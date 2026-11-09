package com.setmon.core.data.calendar

import com.setmon.core.network.ApiResult
import com.setmon.core.network.calendar.CalendarRemoteDataSource
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultCalendarRepository @Inject constructor(
    private val remote: CalendarRemoteDataSource,
) : CalendarRepository {

    override suspend fun getMonth(userId: Long, month: YearMonth): CalendarMonthResult =
        when (val result = remote.getMonth(userId, month)) {
            is ApiResult.Success -> CalendarMonthResult.Success(result.data)
            is ApiResult.ServerError -> CalendarMonthResult.Failure(CalendarError.UNKNOWN)
            ApiResult.NetworkError -> CalendarMonthResult.Failure(CalendarError.NETWORK)
        }
}
