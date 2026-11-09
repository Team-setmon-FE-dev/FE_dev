package com.setmon.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.setmon.core.designsystem.theme.SetmonTheme

enum class SetmonButtonType { Primary, Secondary, Text }

/**
 * 공통 버튼. 피그마 `Android Button` (type=Primary/Secondary/Text, state=Default/Pressed/Disabled).
 * 눌림 상태는 Material 리플이, 비활성은 [enabled] 가 처리한다. 높이 52, 모서리 12.
 * [loading] 이면 글자 대신 작은 로딩 원이 돌고 눌리지 않는다 (로그인·가입 요청 중).
 */
@Composable
fun SetmonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: SetmonButtonType = SetmonButtonType.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val colors = SetmonTheme.colors
    val shape = SetmonTheme.shapes.button
    val style = SetmonTheme.typography.subtitle
    val padding = PaddingValues(horizontal = 20.dp)
    val sized = modifier.height(52.dp)
    // 로딩 중에도 색은 그대로 두고(비활성 회색이 되지 않게) 누름만 막는다
    val onClickGuarded = { if (!loading) onClick() }
    val label: @Composable () -> Unit = {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = if (type == SetmonButtonType.Primary) colors.onPrimary else colors.primary,
            )
        } else {
            Text(text, style = style)
        }
    }

    when (type) {
        SetmonButtonType.Primary -> Button(
            onClick = onClickGuarded,
            modifier = sized,
            enabled = enabled,
            shape = shape,
            contentPadding = padding,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
                disabledContainerColor = colors.outline,
                disabledContentColor = colors.textSecondary,
            ),
        ) { label() }

        SetmonButtonType.Secondary -> OutlinedButton(
            onClick = onClickGuarded,
            modifier = sized,
            enabled = enabled,
            shape = shape,
            contentPadding = padding,
            border = BorderStroke(1.dp, if (enabled) colors.primary else colors.outline),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = colors.surface,
                contentColor = colors.primary,
                disabledContainerColor = colors.surface,
                disabledContentColor = colors.textSecondary,
            ),
        ) { label() }

        SetmonButtonType.Text -> TextButton(
            onClick = onClickGuarded,
            modifier = sized,
            enabled = enabled,
            shape = shape,
            contentPadding = padding,
            colors = ButtonDefaults.textButtonColors(
                contentColor = colors.primary,
                disabledContentColor = colors.textSecondary,
            ),
        ) { label() }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E6)
@Composable
private fun SetmonButtonPreview() {
    SetmonTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SetmonButton("Record a moment", onClick = {}, modifier = Modifier.fillMaxWidth())
            SetmonButton("Secondary", onClick = {}, type = SetmonButtonType.Secondary, modifier = Modifier.fillMaxWidth())
            SetmonButton("Text", onClick = {}, type = SetmonButtonType.Text)
            SetmonButton("Disabled", onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth())
        }
    }
}
