package com.setmon.core.network.friend

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * 친구 API (BE_dev FriendController).
 * 204 응답은 본문이 없어서 Response<Unit> 으로 받고 성공 여부를 직접 확인한다.
 */
internal interface FriendApi {
    @POST("api/friends/requests")
    suspend fun sendRequest(@Body body: FriendCodeRequest): FriendRequestResultResponse

    @GET("api/friends/requests/received")
    suspend fun getReceivedRequests(): List<ReceivedRequestResponse>

    @POST("api/friends/requests/{requestId}/accept")
    suspend fun accept(@Path("requestId") requestId: Long): Response<Unit>

    @POST("api/friends/requests/{requestId}/reject")
    suspend fun reject(@Path("requestId") requestId: Long): Response<Unit>

    @GET("api/friends")
    suspend fun getFriends(): List<FriendResponse>

    @DELETE("api/friends/{friendUserId}")
    suspend fun remove(@Path("friendUserId") friendUserId: Long): Response<Unit>
}

@Serializable
internal data class FriendCodeRequest(val code: String)

/** status: PENDING(요청 보냄) 또는 ACCEPTED(상대가 먼저 요청해 둬서 바로 친구) */
@Serializable
internal data class FriendRequestResultResponse(val requestId: Long, val status: String)

@Serializable
internal data class ReceivedRequestResponse(val requestId: Long, val userId: Long, val nickname: String)

@Serializable
internal data class FriendResponse(val userId: Long, val nickname: String)
