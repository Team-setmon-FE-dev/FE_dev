package com.setmon.app.push

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.setmon.core.data.auth.AuthRepository
import com.setmon.core.data.user.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * FCM 토큰을 서버(PUT /api/users/me/device-token)에 등록한다.
 * - 로그인·가입 직후, 그리고 앱을 켤 때마다(저장된 로그인으로 달력에 들어갈 때) [registerCurrentToken]
 * - 토큰이 바뀔 때 [SetmonMessagingService.onNewToken] → [register]
 * 같은 토큰을 여러 번 보내도 서버가 괜찮다고 해서 매번 보낸다.
 * Logcat 검색창에 `SetmonFCM` 으로 걸러 보면 된다.
 */
@Singleton
class PushTokenRegistrar @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) {
    // 화면이 닫혀도 등록 요청은 끝까지 가게 앱 전체 범위에서 돈다
    private val scope = CoroutineScope(SupervisorJob())

    fun registerCurrentToken() {
        // google-services.json 이 없으면 Firebase 가 초기화되지 않아 getInstance() 가 앱을 죽인다
        if (FirebaseApp.getApps(context).isEmpty()) {
            Log.w(TAG, "Firebase 미설정: app/google-services.json 을 넣고 다시 빌드하세요")
            return
        }
        scope.launch {
            val token = currentFcmToken() ?: return@launch
            send(token)
        }
    }

    /** 새 토큰. 로그인 상태일 때만 서버에 보낸다 (로그아웃 상태면 다음 로그인 때 등록된다) */
    fun register(token: String) {
        scope.launch { send(token) }
    }

    /** Firebase 의 콜백 방식 Task 를 기다린다. 실패하면 null */
    private suspend fun currentFcmToken(): String? = suspendCancellableCoroutine { cont ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                cont.resume(task.result)
            } else {
                Log.w(TAG, "FCM 토큰을 받지 못했어요", task.exception)
                cont.resume(null)
            }
        }
    }

    private suspend fun send(token: String) {
        Log.d(TAG, "FCM token = $token")
        if (authRepository.savedUserId() == null) {
            Log.d(TAG, "로그인 전이라 서버 등록은 건너뜀")
            return
        }
        val ok = userRepository.registerDeviceToken(token)
        Log.d(TAG, if (ok) "기기 토큰 서버 등록 성공" else "기기 토큰 서버 등록 실패 (다음 앱 실행 때 다시 시도)")
    }

    companion object {
        const val TAG = "SetmonFCM"
    }
}
