package com.setmon.feature.day

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.setmon.core.data.vlog.VlogError
import com.setmon.core.data.vlog.VlogRepository
import com.setmon.core.data.vlog.VlogRequestResult
import com.setmon.core.model.todayInKorea
import com.setmon.feature.day.navigation.DayRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

internal data class DayUiState(
    /** 내 달력의 오늘 화면에서만 만들 수 있다 */
    val canMakeVlog: Boolean = false,
    val isRequesting: Boolean = false,
    @param:StringRes val message: Int? = null,
    val messageIsError: Boolean = false,
)

@HiltViewModel
internal class DayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vlogRepository: VlogRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        savedStateHandle.toRoute<DayRoute>().let { route ->
            DayUiState(canMakeVlog = !route.readOnly && route.date == todayInKorea().toString())
        },
    )
    val uiState: StateFlow<DayUiState> = _uiState.asStateFlow()

    fun makeVlog() {
        val state = _uiState.value
        if (!state.canMakeVlog || state.isRequesting) return
        _uiState.update { it.copy(isRequesting = true, message = null) }
        viewModelScope.launch {
            val result = vlogRepository.requestTodayVlog()
            val message = when (result) {
                VlogRequestResult.Started -> R.string.day_vlog_started
                VlogRequestResult.AlreadyDone -> R.string.day_vlog_already_done
                is VlogRequestResult.Failure -> result.error.messageRes()
            }
            _uiState.update {
                it.copy(
                    isRequesting = false,
                    message = message,
                    messageIsError = result is VlogRequestResult.Failure,
                )
            }
        }
    }

    private fun VlogError.messageRes(): Int = when (this) {
        VlogError.NO_CLIPS -> R.string.day_vlog_error_no_clips
        VlogError.NETWORK -> R.string.day_vlog_error_network
        VlogError.UNKNOWN -> R.string.day_vlog_error_unknown
    }
}
