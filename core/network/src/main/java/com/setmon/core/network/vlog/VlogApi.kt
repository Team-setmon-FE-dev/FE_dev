package com.setmon.core.network.vlog

import kotlinx.serialization.Serializable
import retrofit2.http.POST

internal interface VlogApi {
    @POST("api/users/me/days/today/vlog")
    suspend fun requestTodayVlog(): VlogStatusResponse
}

@Serializable
internal data class VlogStatusResponse(val status: String)
