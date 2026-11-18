package com.setmon.core.data.user

import com.setmon.core.model.MyProfile
import com.setmon.core.model.NotificationSettings
import com.setmon.core.network.ApiResult
import com.setmon.core.network.user.UserRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultUserRepository @Inject constructor(
    private val remote: UserRemoteDataSource,
) : UserRepository {

    override suspend fun getMyProfile(): MyProfileResult = remote.getMe().toResult()

    override suspend fun updateSettings(settings: NotificationSettings): MyProfileResult =
        remote.updateSettings(settings).toResult()

    override suspend fun withdraw(): Boolean = remote.withdraw() is ApiResult.Success

    private fun ApiResult<MyProfile>.toResult(): MyProfileResult = when (this) {
        is ApiResult.Success -> MyProfileResult.Success(data)
        is ApiResult.ServerError -> MyProfileResult.Failure(MyProfileError.UNKNOWN)
        ApiResult.NetworkError -> MyProfileResult.Failure(MyProfileError.NETWORK)
    }

    override suspend fun registerDeviceToken(token: String): Boolean =
        remote.registerDeviceToken(token) is ApiResult.Success
}
