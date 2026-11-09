package com.setmon.core.network.user

import com.setmon.core.model.MyProfile
import com.setmon.core.network.ApiResult
import com.setmon.core.network.ServerClient
import com.setmon.core.network.apiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRemoteDataSource @Inject constructor(
    private val serverClient: ServerClient,
) {
    private val api: UserApi by lazy { serverClient.retrofit.create(UserApi::class.java) }

    suspend fun getMe(): ApiResult<MyProfile> = apiCall {
        val me = api.getMe()
        MyProfile(
            id = me.id,
            email = me.email,
            nickname = me.nickname,
            friendCode = me.friendCode,
            totalRecordDays = me.totalRecordDays,
        )
    }
}
