package com.setmon.feature.auth

import androidx.compose.runtime.Composable
import com.setmon.core.designsystem.component.PlaceholderButton
import com.setmon.core.designsystem.component.PlaceholderScreen

@Composable
internal fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSignupClick: () -> Unit,
) {
    PlaceholderScreen(screenId = "S-01", title = "로그인") {
        PlaceholderButton(text = "로그인 (Mock)", onClick = onLoginSuccess)
        PlaceholderButton(text = "회원가입으로", onClick = onSignupClick)
    }
}

@Composable
internal fun SignupScreen(
    onSignupComplete: () -> Unit,
    onBack: () -> Unit,
) {
    PlaceholderScreen(screenId = "S-02", title = "회원가입") {
        PlaceholderButton(text = "가입 완료 (Mock)", onClick = onSignupComplete)
        PlaceholderButton(text = "뒤로", onClick = onBack)
    }
}
