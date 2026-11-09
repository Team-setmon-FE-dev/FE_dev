package com.setmon.core.network.user

import kotlinx.serialization.Serializable
import retrofit2.http.GET

internal interface UserApi {
    @GET("api/users/me")
    suspend fun getMe(): MeResponse
}

/** 알림 설정 값도 함께 오지만 S-12 설정 연결(3주차) 전까지는 쓰지 않는다 */
@Serializable
internal data class MeResponse(
    val id: Long,
    val email: String,
    val nickname: String,
    val friendCode: String,
    val totalRecordDays: Long,
)
