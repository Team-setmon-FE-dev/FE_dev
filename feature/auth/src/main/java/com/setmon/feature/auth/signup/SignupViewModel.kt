package com.setmon.feature.auth.signup

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setmon.core.data.auth.AuthError
import com.setmon.core.data.auth.AuthRepository
import com.setmon.core.data.auth.AuthResult
import com.setmon.feature.auth.AuthValidation
import com.setmon.feature.auth.R
import com.setmon.feature.auth.messageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 가입은 두 단계: 입력(Form) → 가입 성공 후 권한 안내(Permissions, 피그마 `S-02 · Permissions`) */
internal enum class SignupStep { Form, Permissions }

internal data class SignupUiState(
    val email: String = "",
    val password: String = "",
    val nickname: String = "",
    @param:StringRes val emailError: Int? = null,
    @param:StringRes val passwordError: Int? = null,
    @param:StringRes val nicknameError: Int? = null,
    /** 이메일 중복 외의 서버 오류 (네트워크 등) */
    @param:StringRes val formError: Int? = null,
    val isLoading: Boolean = false,
    val step: SignupStep = SignupStep.Form,
) {
    val canSubmit: Boolean get() = email.isNotBlank() && password.isNotEmpty() && nickname.isNotBlank()
}

@HiltViewModel
internal class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, emailError = null, formError = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, passwordError = null, formError = null) }

    fun onNicknameChange(value: String) =
        _uiState.update { it.copy(nickname = value.take(AuthValidation.NICKNAME_MAX), nicknameError = null, formError = null) }

    fun submit() {
        val state = _uiState.value
        if (!state.canSubmit || state.isLoading) return

        val emailError = if (AuthValidation.isValidEmail(state.email)) null else R.string.auth_error_email_format
        val passwordError = if (AuthValidation.isValidPassword(state.password)) null else R.string.auth_error_password_length
        val nicknameError = if (AuthValidation.isValidNickname(state.nickname)) null else R.string.auth_error_nickname_length
        if (emailError != null || passwordError != null || nicknameError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError, nicknameError = nicknameError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, formError = null) }
        viewModelScope.launch {
            val result = authRepository.signup(state.email.trim(), state.password, state.nickname.trim())
            _uiState.update {
                when (result) {
                    is AuthResult.Success -> it.copy(isLoading = false, step = SignupStep.Permissions)
                    // 이메일 중복은 이메일 칸 아래에, 나머지는 버튼 위에
                    is AuthResult.Failure -> if (result.error == AuthError.EMAIL_DUPLICATED) {
                        it.copy(isLoading = false, emailError = result.error.messageRes())
                    } else {
                        it.copy(isLoading = false, formError = result.error.messageRes())
                    }
                }
            }
        }
    }
}
