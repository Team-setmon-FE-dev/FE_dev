package com.setmon.core.data.user

import com.setmon.core.model.MyProfile

/** 내 정보 창구. 화면(feature:mypage)은 이 인터페이스만 본다. */
interface UserRepository {
    suspend fun getMyProfile(): MyProfileResult
}

sealed interface MyProfileResult {
    data class Success(val profile: MyProfile) : MyProfileResult
    data class Failure(val error: MyProfileError) : MyProfileResult
}

enum class MyProfileError { NETWORK, UNKNOWN }
