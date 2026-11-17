package com.setmon.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.setmon.app.navigation.SetmonNavHost
import com.setmon.app.push.PushTarget
import com.setmon.app.push.PushTokenRegistrar
import com.setmon.core.designsystem.theme.SetmonTheme
import com.setmon.core.network.auth.SessionEvents
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var sessionEvents: SessionEvents
    @Inject lateinit var pushTokenRegistrar: PushTokenRegistrar

    /** 알림을 눌러 들어왔을 때 열 화면. NavHost 가 로그인 상태를 확인한 뒤 열고 비운다 */
    private val pendingPush = MutableStateFlow<PushTarget?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        // S-00 시스템 스플래시(배경 + 로고). 첫 화면(브랜드 인트로)이 그려지면 바로 사라진다.
        // 반드시 super.onCreate 보다 먼저 호출해야 한다.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 화면 회전 등으로 다시 만들어질 때는 이미 처리한 알림을 또 열지 않는다
        if (savedInstanceState == null) pendingPush.value = PushTarget.from(intent)
        setContent {
            SetmonTheme {
                SetmonNavHost(
                    sessionExpired = sessionEvents.expired,
                    pendingPush = pendingPush,
                    onPushHandled = { pendingPush.value = null },
                    onSignedIn = pushTokenRegistrar::registerCurrentToken,
                )
            }
        }
    }

    /** 앱이 이미 떠 있을 때 알림을 누르면 여기로 온다 (manifest launchMode="singleTop") */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        PushTarget.from(intent)?.let { pendingPush.value = it }
    }
}
