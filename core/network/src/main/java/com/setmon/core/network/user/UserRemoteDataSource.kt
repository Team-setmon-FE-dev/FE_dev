package com.setmon.core.network.user

import com.setmon.core.model.MyProfile
import com.setmon.core.model.NotificationSettings
import com.setmon.core.network.ApiResult
import com.setmon.core.network.ServerClient
import com.setmon.core.network.apiCall
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRemoteDataSource @Inject constructor(
    private val serverClient: ServerClient,
) {
    private val api: UserApi by lazy { serverClient.retrofit.create(UserApi::class.java) }

    suspend fun getMe(): ApiResult<MyProfile> = apiCall { api.getMe().toProfile() }

    suspend fun updateSettings(settings: NotificationSettings): ApiResult<MyProfile> = apiCall {
        api.updateSettings(
            UpdateSettingsRequest(
                notifyStartHour = settings.notifyStartHour,
                notifyEndHour = settings.notifyEndHour,
                captureNotifyOn = settings.captureNotifyOn,
                completeNotifyOn = settings.completeNotifyOn,
            ),
        ).toProfile()
    }

    suspend fun withdraw(): ApiResult<Unit> = apiCall {
        val response = api.withdraw()
        if (!response.isSuccessful) throw HttpException(response)
    }

    private fun MeResponse.toProfile() = MyProfile(
        id = id,
        email = email,
        nickname = nickname,
        friendCode = friendCode,
        totalRecordDays = totalRecordDays,
        settings = NotificationSettings(notifyStartHour, notifyEndHour, captureNotifyOn, completeNotifyOn),
    )

    /** FCM 토큰을 서버에 등록한다 (로그인 직후, 토큰이 바뀔 때) */
    suspend fun registerDeviceToken(token: String): ApiResult<Unit> = apiCall {
        val response = api.registerDeviceToken(DeviceTokenRequest(token))
        if (!response.isSuccessful) throw HttpException(response)
    }
}
