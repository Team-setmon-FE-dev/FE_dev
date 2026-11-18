package com.setmon.core.model

/** S-12 에 보여줄 내 정보 (GET /api/users/me) */
data class MyProfile(
    val id: Long,
    val email: String,
    val nickname: String,
    val friendCode: String,
    val totalRecordDays: Long,
    val settings: NotificationSettings = NotificationSettings(),
)

/** 촬영 알림 시간대(KST, 둘 다 포함)와 알림 켜고 끄기 (PUT /api/users/me/settings) */
data class NotificationSettings(
    val notifyStartHour: Int = 9,
    val notifyEndHour: Int = 23,
    val captureNotifyOn: Boolean = true,
    val completeNotifyOn: Boolean = true,
)
