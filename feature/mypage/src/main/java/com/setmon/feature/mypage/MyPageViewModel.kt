package com.setmon.feature.mypage

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setmon.core.data.auth.AuthRepository
import com.setmon.core.data.user.MyProfileError
import com.setmon.core.data.user.MyProfileResult
import com.setmon.core.data.user.UserRepository
import com.setmon.core.model.MyProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

internal sealed interface MyPageContent {
    data object Loading : MyPageContent
    data class Error(@param:StringRes val message: Int) : MyPageContent
    data class Loaded(val profile: MyProfile) : MyPageContent
}

internal data class MyPageUiState(
    val content: MyPageContent = MyPageContent.Loading,
    val isLoggingOut: Boolean = false,
    val logoutFailed: Boolean = false,
    val loggedOut: Boolean = false,
)

@HiltViewModel
internal class MyPageViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(content = MyPageContent.Loading) }
        viewModelScope.launch {
            val content = when (val result = userRepository.getMyProfile()) {
                is MyProfileResult.Success -> MyPageContent.Loaded(result.profile)
                is MyProfileResult.Failure -> MyPageContent.Error(result.error.messageRes())
            }
            _uiState.update { it.copy(content = content) }
        }
    }

    fun logout() {
        if (_uiState.value.isLoggingOut) return
        _uiState.update { it.copy(isLoggingOut = true, logoutFailed = false) }
        viewModelScope.launch {
            val success = authRepository.logout()
            _uiState.update {
                it.copy(isLoggingOut = false, logoutFailed = !success, loggedOut = success)
            }
        }
    }

    private fun MyProfileError.messageRes(): Int = when (this) {
        MyProfileError.NETWORK -> R.string.mypage_error_network
        MyProfileError.UNKNOWN -> R.string.mypage_error_unknown
    }
}
