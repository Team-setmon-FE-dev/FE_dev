package com.setmon.feature.friend

import androidx.compose.runtime.Composable
import com.setmon.core.designsystem.component.PlaceholderButton
import com.setmon.core.designsystem.component.PlaceholderScreen

// 서버 연결 전 예시 친구 id
private const val SAMPLE_FRIEND_USER_ID = 2L

@Composable
internal fun FriendScreen(
    onFriendCalendarClick: (friendUserId: Long) -> Unit,
    onBack: () -> Unit,
) {
    PlaceholderScreen(screenId = "S-07", title = "친구") {
        PlaceholderButton(text = "친구(userId=2) 달력 보기") { onFriendCalendarClick(SAMPLE_FRIEND_USER_ID) }
        PlaceholderButton(text = "뒤로", onClick = onBack)
    }
}
