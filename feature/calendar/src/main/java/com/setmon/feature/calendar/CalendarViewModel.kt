package com.setmon.feature.calendar

import android.content.Intent
import android.graphics.Bitmap
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.setmon.core.data.calendar.CalendarError
import com.setmon.core.data.calendar.CalendarMonthResult
import com.setmon.core.data.calendar.CalendarRepository
import com.setmon.core.data.friend.FriendRepository
import com.setmon.core.data.friend.FriendResult
import com.setmon.core.model.CalendarDaySummary
import com.setmon.core.model.Friend
import com.setmon.core.model.todayInKorea
import com.setmon.feature.calendar.navigation.CalendarRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    /** 책갈피에 보이는 친구 줄 (내 달력·친구 달력 모두) */
    val friends: List<Friend> = emptyList(),
    val isLoading: Boolean = false,
    @param:StringRes val error: Int? = null,
    /** 달력 사진 저장·공유 */
    val isSavingImage: Boolean = false,
    @param:StringRes val imageMessage: Int? = null,
    val imageMessageIsError: Boolean = false,
    /** Android 8~9 저장 권한을 화면이 요청해야 함 */
    val needsStoragePermission: Boolean = false,
    /** 화면이 띄울 공유 시트 */
    val shareIntent: Intent? = null,
)

@HiltViewModel
internal class CalendarViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val calendarRepository: CalendarRepository,
    private val friendRepository: FriendRepository,
    private val imageSaver: CalendarImageSaver,
) : ViewModel() {

    private val route: CalendarRoute = savedStateHandle.toRoute<CalendarRoute>()
    private val userId: Long = route.userId
    private val loadedMonths = mutableMapOf<YearMonth, List<CalendarDaySummary>>()
    private var loadJob: Job? = null

    private val _uiState = MutableStateFlow(
        todayInKorea().let { today ->
            val month = YearMonth.from(today)
            CalendarUiState(month = month, today = today, weeks = buildCalendarWeeks(month, today, emptyList()))
        },
    )
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    /** 화면에 돌아올 때마다 부른다. 촬영 후 새 클립과 만료된 사진 주소가 여기서 갱신된다 */
    fun refresh() {
        val today = todayInKorea()
        _uiState.update { it.copy(today = today) }
        val visibleMonth = _uiState.value.month
        load(visibleMonth)

        val currentMonth = YearMonth.from(today)
        if (currentMonth != visibleMonth) refreshCurrentMonth(currentMonth)

        refreshFriends()
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

    /** 친구 화면에서 추가·삭제하고 돌아오면 탭이 바뀌어야 하므로 화면에 올 때마다 부른다. 실패하면 기존 탭 유지 */
    private fun refreshFriends() {
        viewModelScope.launch {
            val result = friendRepository.getFriends()
            if (result is FriendResult.Success) _uiState.update { it.copy(friends = result.data) }
        }
    }

    // ---- 한 달 달력 사진 저장·공유 ----

    /** 권한 요청 뒤 이어서 저장하려고 남겨 둔다 */
    private var pendingImage: Pair<Bitmap, Boolean>? = null
    private var messageJob: Job? = null

    /**
     * 화면이 뜬 달력 그림을 저장한다. share = true 면 저장 후 공유 시트.
     * @param image null 이면 화면에서 그림을 뜨지 못한 것
     */
    fun saveImage(image: Bitmap?, backgroundColor: Int, share: Boolean) {
        if (image == null) {
            showImageMessage(R.string.calendar_image_error, isError = true)
            return
        }
        if (_uiState.value.isSavingImage) return
        _uiState.update { it.copy(isSavingImage = true, imageMessage = null) }
        viewModelScope.launch {
            val month = _uiState.value.month
            when (val result = imageSaver.save(image, backgroundColor, fileName = "setmon_$month")) {
                is CalendarImageResult.Success -> {
                    pendingImage = null
                    _uiState.update { it.copy(isSavingImage = false) }
                    if (share) {
                        _uiState.update { it.copy(shareIntent = imageSaver.shareIntent(result.uri, title = "setmon $month")) }
                    } else {
                        showImageMessage(R.string.calendar_image_saved, isError = false)
                    }
                }
                CalendarImageResult.PermissionRequired -> {
                    pendingImage = image to share
                    _uiState.update { it.copy(isSavingImage = false, needsStoragePermission = true) }
                }
                CalendarImageResult.Failure -> {
                    _uiState.update { it.copy(isSavingImage = false) }
                    showImageMessage(R.string.calendar_image_error, isError = true)
                }
            }
        }
    }

    fun onStoragePermissionResult(granted: Boolean, backgroundColor: Int) {
        _uiState.update { it.copy(needsStoragePermission = false) }
        val pending = pendingImage
        pendingImage = null
        when {
            !granted -> showImageMessage(R.string.calendar_image_error_permission, isError = true)
            pending != null -> saveImage(pending.first, backgroundColor, pending.second)
        }
    }

    fun onShareShown() = _uiState.update { it.copy(shareIntent = null) }

    /** 안내 문구는 3초 뒤 사라진다 */
    private fun showImageMessage(@StringRes message: Int, isError: Boolean) {
        _uiState.update { it.copy(imageMessage = message, imageMessageIsError = isError) }
        messageJob?.cancel()
        messageJob = viewModelScope.launch {
            delay(MESSAGE_MILLIS)
            _uiState.update { it.copy(imageMessage = null) }
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

private const val MESSAGE_MILLIS = 3_000L
