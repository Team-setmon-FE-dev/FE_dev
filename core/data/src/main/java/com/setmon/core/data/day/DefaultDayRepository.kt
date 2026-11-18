package com.setmon.core.data.day

import com.setmon.core.network.ApiResult
import com.setmon.core.network.calendar.CalendarRemoteDataSource
import com.setmon.core.network.clip.ClipRemoteDataSource
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultDayRepository @Inject constructor(
    private val calendarRemote: CalendarRemoteDataSource,
    private val clipRemote: ClipRemoteDataSource,
) : DayRepository {

    override suspend fun getDay(userId: Long, date: LocalDate): DayResult =
        when (val result = calendarRemote.getDay(userId, date)) {
            is ApiResult.Success -> DayResult.Success(result.data)
            is ApiResult.ServerError -> DayResult.Failure(
                if (result.code == "FORBIDDEN") DayError.FORBIDDEN else DayError.UNKNOWN,
            )
            ApiResult.NetworkError -> DayResult.Failure(DayError.NETWORK)
        }

    override suspend fun deleteClip(clipId: Long): DayError? = when (val result = clipRemote.deleteClip(clipId)) {
        is ApiResult.Success -> null
        // 다른 기기에서 이미 지웠으면 목표는 이룬 것
        is ApiResult.ServerError -> if (result.code == "CLIP_NOT_FOUND") null else DayError.UNKNOWN
        ApiResult.NetworkError -> DayError.NETWORK
    }

    override suspend fun setCover(clipId: Long, cover: Boolean): DayError? =
        when (clipRemote.setCover(clipId, cover)) {
            is ApiResult.Success -> null
            is ApiResult.ServerError -> DayError.UNKNOWN
            ApiResult.NetworkError -> DayError.NETWORK
        }
}
