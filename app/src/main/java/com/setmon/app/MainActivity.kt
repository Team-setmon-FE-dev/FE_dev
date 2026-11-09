package com.setmon.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.setmon.app.navigation.SetmonNavHost
import com.setmon.core.designsystem.theme.SetmonTheme
import com.setmon.core.network.auth.SessionEvents
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var sessionEvents: SessionEvents

    override fun onCreate(savedInstanceState: Bundle?) {
        // S-00 시스템 스플래시(배경 + 로고). 첫 화면(브랜드 인트로)이 그려지면 바로 사라진다.
        // 반드시 super.onCreate 보다 먼저 호출해야 한다.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SetmonTheme {
                SetmonNavHost(sessionExpired = sessionEvents.expired)
            }
        }
    }
}