package com.setmon.feature.auth.login

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

internal data class LoginUiState(
    val email: String = "",
    val password: String = "",
    @param:StringRes val emailError: Int? = null,
    @param:StringRes val passwordError: Int? = null,
    /** 서버가 거절했을 때 (비밀번호 틀림, 네트워크 등) 버튼 위에 보이는 문구 */
    @param:StringRes val formError: Int? = null,
    val isLoading: Boolean = false,
    /** 로그인 성공 시 내 userId. 화면 이동 후 null 로 되돌린다 */
    val loggedInUserId: Long? = null,
) {
    val canSubmit: Boolean get() = email.isNotBlank() && password.isNotEmpty()
}

@HiltViewModel
internal class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, emailError = null, formError = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, passwordError = null, formError = null) }

    fun submit() {
        val state = _uiState.value
        if (!state.canSubmit || state.isLoading) return

        val emailError = if (AuthValidation.isValidEmail(state.email)) null else R.string.auth_error_email_format
        val passwordError = if (AuthValidation.isValidPassword(state.password)) null else R.string.auth_error_password_length
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, formError = null) }
        viewModelScope.launch {
            when (val result = authRepository.login(state.email.trim(), state.password)) {
                is AuthResult.Success -> _uiState.update { it.copy(isLoading = false, loggedInUserId = result.userId) }
                is AuthResult.Failure -> _uiState.update { it.copy(isLoading = false, formError = result.error.messageRes()) }
            }
        }
    }

    /** 화면 이동을 한 번 처리한 뒤 호출해서, 뒤로 돌아왔을 때 다시 이동하지 않게 한다. */
    fun onNavigated() = _uiState.update { it.copy(loggedInUserId = null) }
}
