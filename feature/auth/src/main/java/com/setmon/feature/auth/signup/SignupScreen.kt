package com.setmon.feature.auth.signup

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.Composable
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
import com.setmon.core.designsystem.component.SetmonTopBar
import com.setmon.core.designsystem.theme.SetmonTheme
import com.setmon.feature.auth.R

/** S-02 회원가입. 가입 성공 → 권한 안내 → [onSignupComplete] (S-03 내 달력) */
@Composable
internal fun SignupRoot(
    onSignupComplete: (userId: Long) -> Unit,
    onBack: () -> Unit,
    viewModel: SignupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState.step) {
        SignupStep.Form -> SignupScreen(
            uiState = uiState,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onNicknameChange = viewModel::onNicknameChange,
            onSubmit = viewModel::submit,
            onBack = onBack,
        )
        SignupStep.Permissions -> {
            // 이미 가입됐으니 뒤로 가기로 입력 화면에 돌아가지 않는다
            val userId = checkNotNull(uiState.signedUpUserId)
            BackHandler { onSignupComplete(userId) }
            PermissionScreen(onDone = { onSignupComplete(userId) })
        }
    }
}

@Composable
internal fun SignupScreen(
    uiState: SignupUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onNicknameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
) {
    val colors = SetmonTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .imePadding(),
    ) {
        SetmonTopBar(title = stringResource(R.string.auth_signup_title), onBack = onBack)
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(40.dp))
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
                    errorMessage = uiState.passwordError?.let { stringResource(it) },
                    enabled = !uiState.isLoading,
                    isPassword = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
                SetmonTextField(
                    value = uiState.nickname,
                    onValueChange = onNicknameChange,
                    placeholder = stringResource(R.string.auth_nickname_hint),
                    errorMessage = (uiState.nicknameError ?: uiState.formError)?.let { stringResource(it) },
                    enabled = !uiState.isLoading,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(40.dp))
            SetmonButton(
                text = stringResource(R.string.auth_signup),
                onClick = onSubmit,
                enabled = uiState.canSubmit,
                loading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            SetmonButton(
                text = stringResource(R.string.auth_go_login),
                onClick = onBack,
                type = SetmonButtonType.Text,
                enabled = !uiState.isLoading,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, heightDp = 917, widthDp = 412)
@Composable
private fun SignupScreenPreview() {
    SetmonTheme {
        SignupScreen(
            uiState = SignupUiState(email = "taken@setmon.app", password = "1234", nickname = "y", emailError = R.string.auth_error_email_duplicated, passwordError = R.string.auth_error_password_length, nicknameError = R.string.auth_error_nickname_length),
            onEmailChange = {}, onPasswordChange = {}, onNicknameChange = {}, onSubmit = {}, onBack = {},
        )
    }
}
