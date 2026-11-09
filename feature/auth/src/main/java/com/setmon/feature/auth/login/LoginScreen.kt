package com.setmon.feature.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.setmon.core.designsystem.component.SetmonButton
import com.setmon.core.designsystem.component.SetmonButtonType
import com.setmon.core.designsystem.component.SetmonTextField
import com.setmon.core.designsystem.theme.SetmonTheme
import com.setmon.feature.auth.R

/** S-01 로그인. 피그마 `S-01 · Login`, `S-01 · Login (error)` */
@Composable
internal fun LoginRoot(
    onLoginSuccess: (userId: Long) -> Unit,
    onSignupClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.loggedInUserId) {
        uiState.loggedInUserId?.let { userId ->
            viewModel.onNavigated()
            onLoginSuccess(userId)
        }
    }

    LoginScreen(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::submit,
        onSignupClick = onSignupClick,
    )
}

@Composable
internal fun LoginScreen(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onSignupClick: () -> Unit,
) {
    val colors = SetmonTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(200.dp))
        Text(stringResource(R.string.auth_brand), style = SetmonTheme.typography.display, color = colors.textPrimary)
        Text(stringResource(R.string.auth_tagline), style = SetmonTheme.typography.handwriting, color = colors.textSecondary)
        Spacer(Modifier.height(96.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SetmonTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                placeholder = stringResource(R.string.auth_email_hint),
                errorMessage = uiState.emailError?.let { stringResource(it) },
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            SetmonTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                placeholder = stringResource(R.string.auth_password_hint),
                errorMessage = (uiState.passwordError ?: uiState.formError)?.let { stringResource(it) },
                enabled = !uiState.isLoading,
                isPassword = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(32.dp))
        SetmonButton(
            text = stringResource(R.string.auth_login),
            onClick = onSubmit,
            enabled = uiState.canSubmit,
            loading = uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        SetmonButton(
            text = stringResource(R.string.auth_go_signup),
            onClick = onSignupClick,
            type = SetmonButtonType.Text,
            enabled = !uiState.isLoading,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, heightDp = 917, widthDp = 412)
@Composable
private fun LoginScreenPreview() {
    SetmonTheme {
        LoginScreen(
            uiState = LoginUiState(email = "yeeun@setmon.app", password = "wrongpassword", formError = R.string.auth_error_invalid_credentials),
            onEmailChange = {}, onPasswordChange = {}, onSubmit = {}, onSignupClick = {},
        )
    }
}
