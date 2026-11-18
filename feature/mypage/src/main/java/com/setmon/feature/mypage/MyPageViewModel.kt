package com.setmon.feature.mypage

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setmon.core.data.auth.AuthRepository
import com.setmon.core.data.user.MyProfileError
import com.setmon.core.data.user.MyProfileResult
import com.setmon.core.data.user.UserRepository
import com.setmon.core.model.MyProfile
import com.setmon.core.model.NotificationSettings
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
    /** 알림 설정 저장 결과 안내 (실패·잘못된 시간대) */
    @param:StringRes val settingsError: Int? = null,
    val showWithdrawDialog: Boolean = false,
    val isWithdrawing: Boolean = false,
    val withdrawFailed: Boolean = false,
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

    /**
     * 알림 설정을 바꾸면 바로 서버에 저장한다. 화면은 먼저 바꿔 보여 주고, 실패하면 되돌린다.
     * 서버 알림은 "시작 ≤ 지금 시 ≤ 끝"이라 시작이 끝보다 늦으면 저장하지 않는다.
     */
    fun updateSettings(settings: NotificationSettings) {
        val loaded = _uiState.value.content as? MyPageContent.Loaded ?: return
        val before = loaded.profile
        if (settings.notifyStartHour > settings.notifyEndHour) {
            _uiState.update { it.copy(settingsError = R.string.mypage_error_hours) }
            return
        }
        _uiState.update {
            it.copy(content = MyPageContent.Loaded(before.copy(settings = settings)), settingsError = null)
        }
        viewModelScope.launch {
            when (val result = userRepository.updateSettings(settings)) {
                is MyProfileResult.Success -> _uiState.update { state ->
                    // 그사이 또 바꿨으면 마지막 값을 지킨다
                    val current = (state.content as? MyPageContent.Loaded)?.profile
                    if (current?.settings == settings) state.copy(content = MyPageContent.Loaded(result.profile)) else state
                }
                is MyProfileResult.Failure -> _uiState.update {
                    it.copy(content = MyPageContent.Loaded(before), settingsError = R.string.mypage_error_settings)
                }
            }
        }
    }

    fun askWithdraw() = _uiState.update { it.copy(showWithdrawDialog = true, withdrawFailed = false) }

    fun dismissWithdraw() = _uiState.update { it.copy(showWithdrawDialog = false) }

    /** 탈퇴 → 기기의 토큰 삭제 → 로그인 화면 (loggedOut) */
    fun withdraw() {
        if (_uiState.value.isWithdrawing) return
        _uiState.update { it.copy(showWithdrawDialog = false, isWithdrawing = true, withdrawFailed = false) }
        viewModelScope.launch {
            val withdrawn = userRepository.withdraw()
            if (withdrawn) authRepository.logout()
            _uiState.update { it.copy(isWithdrawing = false, withdrawFailed = !withdrawn, loggedOut = withdrawn) }
        }
    }

    private fun MyProfileError.messageRes(): Int = when (this) {
        MyProfileError.NETWORK -> R.string.mypage_error_network
        MyProfileError.UNKNOWN -> R.string.mypage_error_unknown
    }
}
