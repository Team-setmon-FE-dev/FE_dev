package com.setmon.core.data.friend

import com.setmon.core.model.Friend
import com.setmon.core.model.FriendRequest

/** 친구 창구. 화면(feature:friend)은 이 인터페이스만 본다. */
interface FriendRepository {
    suspend fun getFriends(): FriendResult<List<Friend>>
    suspend fun getReceivedRequests(): FriendResult<List<FriendRequest>>
    suspend fun sendRequest(code: String): FriendResult<FriendRequestOutcome>
    suspend fun accept(requestId: Long): FriendResult<Unit>
    suspend fun reject(requestId: Long): FriendResult<Unit>
    suspend fun remove(friendUserId: Long): FriendResult<Unit>
}

sealed interface FriendResult<out T> {
    data class Success<T>(val data: T) : FriendResult<T>
    data class Failure(val error: FriendError) : FriendResult<Nothing>
}

enum class FriendRequestOutcome {
    /** 요청을 보냈고 상대의 수락을 기다린다 */
    REQUESTED,

    /** 상대가 이미 나에게 요청해 둬서 바로 친구가 됐다 */
    BECAME_FRIENDS,
}

/** 서버 에러 코드와 맞춘다 */
enum class FriendError {
    CODE_NOT_FOUND,
    SELF_REQUEST,
    ALREADY_FRIEND,
    DUPLICATE_REQUEST,
    NETWORK,
    UNKNOWN,
}
