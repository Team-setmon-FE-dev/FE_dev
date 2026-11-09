package com.setmon.core.model

/** S-12 에 보여줄 내 정보 (GET /api/users/me) */
data class MyProfile(
    val id: Long,
    val email: String,
    val nickname: String,
    val friendCode: String,
    val totalRecordDays: Long,
)
