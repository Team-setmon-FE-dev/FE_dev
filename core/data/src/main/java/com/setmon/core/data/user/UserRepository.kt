package com.setmon.core.data.user

import com.setmon.core.model.MyProfile
import com.setmon.core.model.NotificationSettings

/** 내 정보 창구. 화면(feature:mypage)은 이 인터페이스만 본다. */
interface UserRepository {
    suspend fun getMyProfile(): MyProfileResult

    /** 푸시를 받을 FCM 토큰을 서버에 등록한다. 성공하면 true */
    suspend fun registerDeviceToken(token: String): Boolean

    /** 알림 설정 저장. 성공하면 서버에 저장된 내 정보 */
    suspend fun updateSettings(settings: NotificationSettings): MyProfileResult

    /** 탈퇴. 서버에서 지워지면 true (기기의 토큰 삭제는 AuthRepository.logout) */
    suspend fun withdraw(): Boolean
}

sealed interface MyProfileResult {
    data class Success(val profile: MyProfile) : MyProfileResult
    data class Failure(val error: MyProfileError) : MyProfileResult
}

enum class MyProfileError { NETWORK, UNKNOWN }
