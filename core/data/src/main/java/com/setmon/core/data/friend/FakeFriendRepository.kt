package com.setmon.core.data.friend

import com.setmon.core.model.Friend
import com.setmon.core.model.FriendRequest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 서버 연결(3주차) 전까지 쓰는 가짜 구현. 앱을 다시 켜면 처음 상태로 돌아간다.
 *
 * 일부러 다른 결과를 보고 싶을 때 쓰는 코드
 *  - [NOT_FOUND_CODE]: 없는 코드 / [SELF_CODE]: 내 코드 / [FRIEND_CODE]: 이미 친구
 *  - [PENDING_CODE]: 이미 요청함 / [OFFLINE_CODE]: 네트워크 오류
 *  - [REQUESTER_CODE]: 상대가 먼저 요청해 둬서 바로 친구가 됨
 * 그 외 코드는 모두 요청 성공이다.
 */
@Singleton
class FakeFriendRepository @Inject constructor() : FriendRepository {
    private var friends = listOf(Friend(2L, "Minjun"), Friend(3L, "Taeyang"))
    private var requests = listOf(FriendRequest(100L, 4L, "Jisu"))

    override suspend fun getFriends(): FriendResult<List<Friend>> = FriendResult.Success(friends)

    override suspend fun getReceivedRequests(): FriendResult<List<FriendRequest>> = FriendResult.Success(requests)

    override suspend fun sendRequest(code: String): FriendResult<FriendRequestOutcome> = when (code) {
        NOT_FOUND_CODE -> FriendResult.Failure(FriendError.CODE_NOT_FOUND)
        SELF_CODE -> FriendResult.Failure(FriendError.SELF_REQUEST)
        FRIEND_CODE -> FriendResult.Failure(FriendError.ALREADY_FRIEND)
        PENDING_CODE -> FriendResult.Failure(FriendError.DUPLICATE_REQUEST)
        OFFLINE_CODE -> FriendResult.Failure(FriendError.NETWORK)
        REQUESTER_CODE -> {
            requests.firstOrNull()?.let(::moveToFriends)
            FriendResult.Success(FriendRequestOutcome.BECAME_FRIENDS)
        }
        else -> FriendResult.Success(FriendRequestOutcome.REQUESTED)
    }

    override suspend fun accept(requestId: Long): FriendResult<Unit> {
        requests.firstOrNull { it.requestId == requestId }?.let(::moveToFriends)
        return FriendResult.Success(Unit)
    }

    override suspend fun reject(requestId: Long): FriendResult<Unit> {
        requests = requests.filterNot { it.requestId == requestId }
        return FriendResult.Success(Unit)
    }

    override suspend fun remove(friendUserId: Long): FriendResult<Unit> {
        friends = friends.filterNot { it.userId == friendUserId }
        return FriendResult.Success(Unit)
    }

    private fun moveToFriends(request: FriendRequest) {
        requests = requests - request
        friends = (friends + Friend(request.userId, request.nickname)).sortedBy { it.nickname }
    }

    companion object {
        const val NOT_FOUND_CODE = "ZZZ999"
        const val SELF_CODE = "SELF00"
        const val FRIEND_CODE = "MINJUN"
        const val PENDING_CODE = "WAIT00"
        const val OFFLINE_CODE = "OFFLINE"
        const val REQUESTER_CODE = "JISU00"
    }
}
