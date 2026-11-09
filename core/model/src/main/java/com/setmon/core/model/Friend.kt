package com.setmon.core.model

data class Friend(
    val userId: Long,
    val nickname: String,
)

/** 나에게 온 대기 중인 친구 요청 */
data class FriendRequest(
    val requestId: Long,
    val userId: Long,
    val nickname: String,
)
