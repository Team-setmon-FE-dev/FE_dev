package com.setmon.app.push

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

/**
 * 1주차 확인용: 앱 시작 시 FCM 토큰을 Logcat에 찍는다.
 * Logcat 검색창에 `SetmonFCM` 으로 걸러 보면 된다.
 * 3주차에 로그인 직후·토큰 변경 시 PUT /api/users/me/device-token 으로 서버에 등록하는 것으로 바뀐다.
 */
object FcmTokenLogger {
    const val TAG = "SetmonFCM"

    fun logCurrentToken(context: Context) {
        // google-services.json 이 없으면 Firebase 가 초기화되지 않아 getInstance() 가 앱을 죽인다
        if (FirebaseApp.getApps(context).isEmpty()) {
            Log.w(TAG, "Firebase 미설정: app/google-services.json 을 넣고 다시 빌드하세요")
            return
        }
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "FCM token = ${task.result}")
            } else {
                Log.w(TAG, "FCM 토큰을 받지 못했어요", task.exception)
            }
        }
    }
}
