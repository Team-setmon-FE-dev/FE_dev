package com.setmon.feature.capture

import androidx.compose.runtime.Composable
import com.setmon.core.designsystem.component.PlaceholderScreen

/** B가 채울 화면. 지금은 제목만 있다. 시스템 뒤로가기로 달력으로 돌아갈 수 있다. */
@Suppress("UNUSED_PARAMETER")
@Composable
internal fun CaptureScreen(
    onUploadComplete: () -> Unit,
    onBack: () -> Unit,
) {
    PlaceholderScreen(
        screenId = "S-04",
        title = "촬영 화면",
        description = "B 담당 — 녹화·업로드 모듈이 들어올 자리",
    )
}
