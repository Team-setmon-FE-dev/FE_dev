package com.setmon.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SetmonColorScheme = lightColorScheme(
    primary = SetmonPrimary,
    onPrimary = SetmonOnPrimary,
    background = SetmonBackground,
    surface = SetmonSurface,
    onSurface = SetmonOnSurface,
    error = SetmonError,
)

/** 앱 전체를 감싸는 테마. MainActivity와 각 화면의 Preview에서 쓴다. */
@Composable
fun SetmonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SetmonColorScheme,
        content = content,
    )
}
