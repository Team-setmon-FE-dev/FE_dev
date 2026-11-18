package com.setmon.core.network.friend

import com.setmon.core.model.Friend
import com.setmon.core.model.FriendRequest
import com.setmon.core.network.ApiResult
import com.setmon.core.network.ServerClient
import com.setmon.core.network.apiCall
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendRemoteDataSource @Inject constructor(
    private val serverClient: ServerClient,
) {
    private val api: FriendApi by lazy { serverClient.retrofit.create(FriendApi::class.java) }

    /** 성공하면 true = 바로 친구가 됨, false = 요청만 보냄 */
    suspend fun sendRequest(code: String): ApiResult<Boolean> = apiCall {
        api.sendRequest(FriendCodeRequest(code)).status == STATUS_ACCEPTED
    }

    suspend fun getReceivedRequests(): ApiResult<List<FriendRequest>> = apiCall {
        api.getReceivedRequests().map { FriendRequest(it.requestId, it.userId, it.nickname) }
    }

    suspend fun accept(requestId: Long): ApiResult<Unit> = apiCall { api.accept(requestId).orThrow() }

    suspend fun reject(requestId: Long): ApiResult<Unit> = apiCall { api.reject(requestId).orThrow() }

    suspend fun getFriends(): ApiResult<List<Friend>> = apiCall {
        api.getFriends().map { Friend(it.userId, it.nickname) }
    }

    suspend fun remove(friendUserId: Long): ApiResult<Unit> = apiCall { api.remove(friendUserId).orThrow() }

    /** 실패 응답이면 다른 API 와 똑같이 apiCall 이 에러 코드를 읽도록 HttpException 으로 던진다 */
    private fun Response<Unit>.orThrow() {
        if (!isSuccessful) throw HttpException(this)
    }

    private companion object {
        const val STATUS_ACCEPTED = "ACCEPTED"
    }
}
