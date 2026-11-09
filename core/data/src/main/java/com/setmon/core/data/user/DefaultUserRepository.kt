package com.setmon.core.data.user

import com.setmon.core.network.ApiResult
import com.setmon.core.network.user.UserRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultUserRepository @Inject constructor(
    private val remote: UserRemoteDataSource,
) : UserRepository {

    override suspend fun getMyProfile(): MyProfileResult = when (val result = remote.getMe()) {
        is ApiResult.Success -> MyProfileResult.Success(result.data)
        is ApiResult.ServerError -> MyProfileResult.Failure(MyProfileError.UNKNOWN)
        ApiResult.NetworkError -> MyProfileResult.Failure(MyProfileError.NETWORK)
    }
}
