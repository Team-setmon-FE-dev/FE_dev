package com.setmon.app.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * FCM 수신 지점.
 * - 1주차: 토큰 갱신·메시지 도착을 로그로만 남긴다.
 * - 3주차: 알림 종류(촬영 / 완성)별로 알림을 띄우고, 탭하면 S-04 / 해당 날짜 S-05 로 연다.
 *   메시지 형식은 C가 월요일에 넘겨주는 FCM 메시지 형식(type, date)에 맞춘다.
 */
class SetmonMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d(FcmTokenLogger.TAG, "FCM token refreshed = $token")
        // TODO(3주차): 로그인 상태면 PUT /api/users/me/device-token
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(FcmTokenLogger.TAG, "message from=${message.from} data=${message.data} title=${message.notification?.title}")
    }
}
