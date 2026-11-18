package com.setmon.app.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * FCM 수신 지점. 서버 형식은 BE_dev docs/FCM.md (data-only: type, date, title, body)
 * - 토큰이 바뀌면 서버에 다시 등록한다
 * - 메시지가 오면 알림을 띄우고, 탭하면 MainActivity 가 S-04 / 그날 S-05 로 연다
 */
@AndroidEntryPoint
class SetmonMessagingService : FirebaseMessagingService() {
    @Inject lateinit var tokenRegistrar: PushTokenRegistrar
    @Inject lateinit var notifier: PushNotifier

    override fun onNewToken(token: String) {
        Log.d(PushTokenRegistrar.TAG, "FCM token refreshed")
        tokenRegistrar.register(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(PushTokenRegistrar.TAG, "message from=${message.from} data=${message.data}")
        notifier.show(message.data)
    }
}
