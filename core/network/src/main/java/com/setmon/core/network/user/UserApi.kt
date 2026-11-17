package com.setmon.core.network.user

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

internal interface UserApi {
    @GET("api/users/me")
    suspend fun getMe(): MeResponse

    /** 204. 같은 토큰을 여러 번 보내도 된다 */
    @PUT("api/users/me/device-token")
    suspend fun registerDeviceToken(@Body body: DeviceTokenRequest): Response<Unit>
}

@Serializable
internal data class DeviceTokenRequest(val token: String)

/** 알림 설정 값도 함께 오지만 S-12 설정 연결(3주차) 전까지는 쓰지 않는다 */
@Serializable
internal data class MeResponse(
    val id: Long,
    val email: String,
    val nickname: String,
    val friendCode: String,
    val totalRecordDays: Long,
)
