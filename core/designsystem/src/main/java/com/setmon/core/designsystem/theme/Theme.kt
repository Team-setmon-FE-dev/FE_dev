package com.setmon.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

internal val LocalSetmonColors = staticCompositionLocalOf { SetmonColors() }
internal val LocalSetmonTypography = staticCompositionLocalOf { SetmonTypography() }
internal val LocalSetmonSpacing = staticCompositionLocalOf { SetmonSpacing() }
internal val LocalSetmonShapes = staticCompositionLocalOf { SetmonShapes() }

/**
 * 화면에서 토큰을 꺼내는 입구.
 * 예) `color = SetmonTheme.colors.primary`, `style = SetmonTheme.typography.body`
 */
object SetmonTheme {
    val colors: SetmonColors
        @Composable @ReadOnlyComposable get() = LocalSetmonColors.current
    val typography: SetmonTypography
        @Composable @ReadOnlyComposable get() = LocalSetmonTypography.current
    val spacing: SetmonSpacing
        @Composable @ReadOnlyComposable get() = LocalSetmonSpacing.current
    val shapes: SetmonShapes
        @Composable @ReadOnlyComposable get() = LocalSetmonShapes.current
}

/** 앱 전체를 감싸는 테마. MainActivity와 각 화면의 Preview에서 쓴다. */
@Composable
fun SetmonTheme(content: @Composable () -> Unit) {
    val colors = SetmonColors()
    val typography = SetmonTypography()

    // Material3 기본 컴포넌트(TextField, Dialog 등)도 setmon 색·글자를 따라가게 연결한다.
    val colorScheme = lightColorScheme(
        primary = colors.primary,
        onPrimary = colors.onPrimary,
        secondary = colors.secondary,
        onSecondary = colors.textPrimary,
        tertiary = colors.accent,
        onTertiary = colors.textPrimary,
        background = colors.background,
        onBackground = colors.textPrimary,
        surface = colors.surface,
        onSurface = colors.textPrimary,
        surfaceVariant = colors.background,
        onSurfaceVariant = colors.textSecondary,
        outline = colors.outline,
        outlineVariant = colors.outline,
        error = colors.error,
        onError = colors.onPrimary,
    )
    val materialTypography = Typography(
        displayLarge = typography.display,
        titleLarge = typography.title,
        titleMedium = typography.subtitle,
        bodyLarge = typography.body,
        bodyMedium = typography.body,
        labelLarge = typography.label,
        labelMedium = typography.label,
        labelSmall = typography.caption,
    )

    CompositionLocalProvider(
        LocalSetmonColors provides colors,
        LocalSetmonTypography provides typography,
        LocalSetmonSpacing provides SetmonSpacing(),
        LocalSetmonShapes provides SetmonShapes(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = materialTypography,
            content = content,
        )
    }
}
