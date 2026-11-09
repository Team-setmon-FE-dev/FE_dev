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
}

@Serializable
internal data class MonthResponse(val days: List<DaySummaryResponse>)

@Serializable
internal data class DaySummaryResponse(
    val date: String,
    val thumbnailUrl: String? = null,
    val clipCount: Int,
)
