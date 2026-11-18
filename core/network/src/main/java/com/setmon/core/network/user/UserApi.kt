package com.setmon.core.network.user

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT

internal interface UserApi {
    @GET("api/users/me")
    suspend fun getMe(): MeResponse

    /** 204. 같은 토큰을 여러 번 보내도 된다 */
    @PUT("api/users/me/device-token")
    suspend fun registerDeviceToken(@Body body: DeviceTokenRequest): Response<Unit>

    /** 저장 후 바뀐 내 정보를 돌려준다 */
    @PUT("api/users/me/settings")
    suspend fun updateSettings(@Body body: UpdateSettingsRequest): MeResponse

    /** 탈퇴 (클립·브이로그·친구·기기 토큰 모두 삭제). 204 */
    @DELETE("api/users/me")
    suspend fun withdraw(): Response<Unit>
}

@Serializable
internal data class DeviceTokenRequest(val token: String)

@Serializable
internal data class MeResponse(
    val id: Long,
    val email: String,
    val nickname: String,
    val friendCode: String,
    val totalRecordDays: Long,
    val notifyStartHour: Int = 9,
    val notifyEndHour: Int = 23,
    val captureNotifyOn: Boolean = true,
    val completeNotifyOn: Boolean = true,
)

/** 네 값 모두 필수. 시각은 KST 0~23, 둘 다 포함 */
@Serializable
internal data class UpdateSettingsRequest(
    val notifyStartHour: Int,
    val notifyEndHour: Int,
    val captureNotifyOn: Boolean,
    val completeNotifyOn: Boolean,
)
