package com.setmon.core.data.friend

import com.setmon.core.model.Friend
import com.setmon.core.model.FriendRequest
import com.setmon.core.network.ApiResult
import com.setmon.core.network.friend.FriendRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

/** 실서버 친구 구현 (3주차). 에러 코드는 BE_dev ErrorCode 기준 */
@Singleton
class DefaultFriendRepository @Inject constructor(
    private val remote: FriendRemoteDataSource,
) : FriendRepository {

    override suspend fun getFriends(): FriendResult<List<Friend>> = remote.getFriends().toFriendResult()

    override suspend fun getReceivedRequests(): FriendResult<List<FriendRequest>> =
        remote.getReceivedRequests().toFriendResult()

    override suspend fun sendRequest(code: String): FriendResult<FriendRequestOutcome> =
        when (val result = remote.sendRequest(code.trim().uppercase())) {
            is ApiResult.Success -> FriendResult.Success(
                if (result.data) FriendRequestOutcome.BECAME_FRIENDS else FriendRequestOutcome.REQUESTED,
            )
            is ApiResult.ServerError -> FriendResult.Failure(friendErrorOf(result.code))
            ApiResult.NetworkError -> FriendResult.Failure(FriendError.NETWORK)
        }

    override suspend fun accept(requestId: Long): FriendResult<Unit> = remote.accept(requestId).toFriendResult()

    override suspend fun reject(requestId: Long): FriendResult<Unit> = remote.reject(requestId).toFriendResult()

    override suspend fun remove(friendUserId: Long): FriendResult<Unit> = remote.remove(friendUserId).toFriendResult()

    private fun <T> ApiResult<T>.toFriendResult(): FriendResult<T> = when (this) {
        is ApiResult.Success -> FriendResult.Success(data)
        is ApiResult.ServerError -> FriendResult.Failure(friendErrorOf(code))
        ApiResult.NetworkError -> FriendResult.Failure(FriendError.NETWORK)
    }
}

internal fun friendErrorOf(code: String?): FriendError = when (code) {
    "FRIEND_CODE_NOT_FOUND" -> FriendError.CODE_NOT_FOUND
    "SELF_FRIEND_REQUEST" -> FriendError.SELF_REQUEST
    "ALREADY_FRIEND" -> FriendError.ALREADY_FRIEND
    "DUPLICATE_REQUEST" -> FriendError.DUPLICATE_REQUEST
    // 상대가 요청을 취소했거나 이미 친구를 끊은 경우 등. 화면은 목록을 새로 불러온다
    "REQUEST_NOT_FOUND", "FRIEND_NOT_FOUND" -> FriendError.GONE
    else -> FriendError.UNKNOWN
}
