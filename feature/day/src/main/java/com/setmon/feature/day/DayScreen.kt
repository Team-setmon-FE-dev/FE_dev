package com.setmon.feature.day

import androidx.compose.runtime.Composable
import com.setmon.core.designsystem.component.PlaceholderButton
import com.setmon.core.designsystem.component.PlaceholderScreen

@Composable
internal fun DayScreen(
    userId: Long,
    date: String,
    readOnly: Boolean,
    onBack: () -> Unit,
) {
    PlaceholderScreen(
        screenId = "S-05",
        title = "하루 상세",
        description = "userId = $userId, date = $date, readOnly = $readOnly",
    ) {
        PlaceholderButton(text = "뒤로", onClick = onBack)
    }
}
