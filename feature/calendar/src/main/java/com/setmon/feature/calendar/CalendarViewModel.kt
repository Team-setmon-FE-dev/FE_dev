package com.setmon.feature.calendar

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.setmon.core.data.calendar.CalendarError
import com.setmon.core.data.calendar.CalendarMonthResult
import com.setmon.core.data.calendar.CalendarRepository
import com.setmon.core.model.CalendarDaySummary
import com.setmon.feature.calendar.navigation.CalendarRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

internal data class CalendarUiState(
    val month: YearMonth,
    val today: LocalDate,
    val weeks: List<List<CalendarCell>>,
    val todayClipCount: Int = 0,
    val isLoading: Boolean = false,
    @param:StringRes val error: Int? = null,
)

@HiltViewModel
internal class CalendarViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val calendarRepository: CalendarRepository,
) : ViewModel() {

    private val userId: Long = savedStateHandle.toRoute<CalendarRoute>().userId
    private val loadedMonths = mutableMapOf<YearMonth, List<CalendarDaySummary>>()
    private var loadJob: Job? = null

    private val _uiState = MutableStateFlow(
        LocalDate.now().let { today ->
            val month = YearMonth.from(today)
            CalendarUiState(month = month, today = today, weeks = buildCalendarWeeks(month, today, emptyList()))
        },
    )
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    /** 화면에 돌아올 때마다 부른다. 촬영 후 새 클립과 만료된 사진 주소가 여기서 갱신된다 */
    fun refresh() {
        val today = LocalDate.now()
        _uiState.update { it.copy(today = today) }
        val visibleMonth = _uiState.value.month
        load(visibleMonth)

        val currentMonth = YearMonth.from(today)
        if (currentMonth != visibleMonth) refreshCurrentMonth(currentMonth)
    }

    fun showPreviousMonth() = showMonth(_uiState.value.month.minusMonths(1))

    fun showNextMonth() = showMonth(_uiState.value.month.plusMonths(1))

    fun retry() = load(_uiState.value.month)

    private fun showMonth(month: YearMonth) {
        _uiState.update { it.copy(month = month) }
        render()
        load(month)
    }

    private fun load(month: YearMonth) {
        loadJob?.cancel()
        _uiState.update { it.copy(isLoading = month !in loadedMonths, error = null) }
        loadJob = viewModelScope.launch {
            when (val result = calendarRepository.getMonth(userId, month)) {
                is CalendarMonthResult.Success -> {
                    loadedMonths[month] = result.days
                    _uiState.update { it.copy(isLoading = false) }
                    render()
                }
                is CalendarMonthResult.Failure -> _uiState.update {
                    it.copy(isLoading = false, error = result.error.messageRes())
                }
            }
        }
    }

    /** 다른 달을 보는 중에도 "오늘" 카드의 클립 수를 맞춘다 */
    private fun refreshCurrentMonth(month: YearMonth) {
        viewModelScope.launch {
            val result = calendarRepository.getMonth(userId, month)
            if (result is CalendarMonthResult.Success) {
                loadedMonths[month] = result.days
                render()
            }
        }
    }

    private fun render() = _uiState.update { state ->
        state.copy(
            weeks = buildCalendarWeeks(state.month, state.today, loadedMonths[state.month].orEmpty()),
            todayClipCount = loadedMonths[YearMonth.from(state.today)]
                ?.firstOrNull { it.date == state.today }
                ?.clipCount
                ?: 0,
        )
    }

    private fun CalendarError.messageRes(): Int = when (this) {
        CalendarError.NETWORK -> R.string.calendar_error_network
        CalendarError.UNKNOWN -> R.string.calendar_error_unknown
    }
}
