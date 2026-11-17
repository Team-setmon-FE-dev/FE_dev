package com.setmon.core.network.calendar

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface CalendarApi {
    /** @param month "2026-11" */
    @GET("api/users/{userId}/days")
    suspend fun getMonth(
        @Path("userId") userId: Long,
        @Query("month") month: String,
    ): MonthResponse

    /** @param date "2026-11-04" */
    @GET("api/users/{userId}/days/{date}")
    suspend fun getDay(
        @Path("userId") userId: Long,
        @Path("date") date: String,
    ): DayDetailResponse
}

@Serializable
internal data class MonthResponse(val days: List<DaySummaryResponse>)

@Serializable
internal data class DaySummaryResponse(
    val date: String,
    val thumbnailUrl: String? = null,
    val clipCount: Int,
)

@Serializable
internal data class DayDetailResponse(
    val userId: Long,
    val date: String,
    val notifyStartHour: Int,
    val notifyEndHour: Int,
    val vlog: VlogInfoResponse? = null,
    val clips: List<ClipItemResponse> = emptyList(),
)

@Serializable
internal data class VlogInfoResponse(
    val status: String,
    val stale: Boolean = false,
    val videoUrl: String? = null,
    val thumbnailUrl: String? = null,
    val failReason: String? = null,
)

@Serializable
internal data class ClipItemResponse(
    val id: Long,
    val slotHour: Int,
    val durationMs: Int = 0,
    val videoUrl: String,
    val thumbnailUrl: String? = null,
)
