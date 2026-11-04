package com.setmon.feature.calendar

import androidx.compose.runtime.Composable
import com.setmon.core.designsystem.component.PlaceholderButton
import com.setmon.core.designsystem.component.PlaceholderScreen
import java.time.LocalDate

@Composable
internal fun CalendarScreen(
    userId: Long,
    readOnly: Boolean,
    onDayClick: (date: String) -> Unit,
    onCaptureClick: () -> Unit,
    onFriendClick: () -> Unit,
    onMyPageClick: () -> Unit,
    onBack: () -> Unit,
) {
    PlaceholderScreen(
        screenId = "S-03",
        title = if (readOnly) "친구 달력 (읽기 전용)" else "메인 달력",
        description = "userId = $userId, readOnly = $readOnly",
    ) {
        PlaceholderButton(text = "오늘 하루 상세 보기") { onDayClick(LocalDate.now().toString()) }
        if (readOnly) {
            // 읽기 전용이면 촬영·친구·마이페이지 대신 뒤로가기만
            PlaceholderButton(text = "뒤로", onClick = onBack)
        } else {
            PlaceholderButton(text = "촬영", onClick = onCaptureClick)
            PlaceholderButton(text = "친구", onClick = onFriendClick)
            PlaceholderButton(text = "마이페이지", onClick = onMyPageClick)
        }
    }
}
