package com.setmon.feature.auth.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setmon.core.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

internal enum class SplashDestination { Login, Calendar }

/**
 * S-00 브랜드 인트로 동안 저장된 로그인이 있는지 확인한다.
 * 확인이 아무리 빨라도 [MIN_SHOW_MS] 동안은 인트로를 보여주고, 그 뒤 갈 곳을 내보낸다.
 */
@HiltViewModel
internal class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            val session = async { runCatching { authRepository.hasSavedSession() }.getOrDefault(false) }
            delay(MIN_SHOW_MS)
            _destination.value = if (session.await()) SplashDestination.Calendar else SplashDestination.Login
        }
    }

    companion object {
        /** 구글 권장: 스플래시는 짧게. 로딩 바 애니메이션 길이와 같다. */
        const val MIN_SHOW_MS = 1_100L
    }
}
