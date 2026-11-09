package com.setmon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.setmon.core.designsystem.theme.SetmonTheme

/**
 * 공통 입력창. 피그마 `TextField` (State=Default/Focused/Error/Disabled). 높이 52, 모서리 12.
 * 오류 문구는 [errorMessage] 가 있으면 입력창 아래에 작은 글씨로 붙는다.
 */
@Composable
fun SetmonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    errorMessage: String? = null,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colors = SetmonTheme.colors
    val shape = SetmonTheme.shapes.button
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val isError = errorMessage != null

    val borderColor = when {
        !enabled -> colors.outline
        isError -> colors.error
        focused -> colors.primary
        else -> colors.outline
    }
    val borderWidth = if (focused && enabled && !isError) 1.5.dp else 1.dp

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            textStyle = SetmonTheme.typography.body.copy(color = colors.textPrimary),
            cursorBrush = SolidColor(colors.primary),
            interactionSource = interaction,
            keyboardActions = keyboardActions,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = if (isPassword) keyboardOptions.copy(keyboardType = KeyboardType.Password) else keyboardOptions,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(if (enabled) colors.surface else colors.background, shape)
                        .border(borderWidth, borderColor, shape)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(placeholder, style = SetmonTheme.typography.body, color = colors.textSecondary)
                    }
                    inner()
                }
            },
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = SetmonTheme.typography.caption,
                color = colors.error,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E6)
@Composable
private fun SetmonTextFieldPreview() {
    SetmonTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SetmonTextField(value = "", onValueChange = {}, placeholder = "이메일")
            SetmonTextField(value = "name@email.com", onValueChange = {}, placeholder = "이메일")
            SetmonTextField(value = "1234", onValueChange = {}, placeholder = "비밀번호", isPassword = true, errorMessage = "비밀번호는 8자 이상이에요")
            SetmonTextField(value = "", onValueChange = {}, placeholder = "닉네임", enabled = false)
        }
    }
}
