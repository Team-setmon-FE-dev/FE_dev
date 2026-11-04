package com.setmon.feature.mypage

import androidx.compose.runtime.Composable
import com.setmon.core.designsystem.component.PlaceholderButton
import com.setmon.core.designsystem.component.PlaceholderScreen

@Composable
internal fun MyPageScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit,
) {
    PlaceholderScreen(screenId = "S-12", title = "마이페이지") {
        PlaceholderButton(text = "로그아웃", onClick = onLogout)
        PlaceholderButton(text = "뒤로", onClick = onBack)
    }
}
