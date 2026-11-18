package com.setmon.feature.day

import android.content.Intent
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.setmon.core.data.day.DayError
import com.setmon.core.data.day.DayRepository
import com.setmon.core.data.day.DayResult
import com.setmon.core.data.media.VlogSaveError
import com.setmon.core.data.media.VlogSaveResult
import com.setmon.core.data.media.VlogSaver
import com.setmon.core.data.vlog.VlogError
import com.setmon.core.data.vlog.VlogRepository
import com.setmon.core.data.vlog.VlogRequestResult
import com.setmon.core.model.DayClip
import com.setmon.core.model.DayDetail
import com.setmon.core.model.DayVlogStatus
import com.setmon.core.model.todayInKorea
import com.setmon.feature.day.navigation.DayRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** 만드는 중일 때 다시 조회하는 간격 */
private const val POLL_INTERVAL_MS = 5_000L

/** 타임라인 한 칸 (알림 시간대의 한 시간) */
internal data class TimelineSlot(val hour: Int, val clip: DayClip?)

/**
 * 플레이어에 넣을 영상들. [key] 가 바뀔 때만 플레이어를 새로 채운다.
 * (주소는 조회할 때마다 새로 발급되므로 주소로 비교하면 5초마다 처음부터 다시 재생된다)
 */
internal data class Playback(
    val key: String,
    val urls: List<String>,
    val thumbnailUrl: String?,
    /** 사용자가 칸을 눌러 고른 클립이면 바로 재생 */
    val autoPlay: Boolean,
)

internal data class DayUiState(
    val date: LocalDate,
    val readOnly: Boolean,
    val friendNickname: String?,
    val isToday: Boolean,
    val isLoading: Boolean = true,
    @param:StringRes val loadError: Int? = null,
    val day: DayDetail? = null,
    /** 타임라인에서 고른 클립. null 이면 브이로그(없으면 클립 전체 이어 재생) */
    val selectedClipId: Long? = null,
    /** 주소가 만료돼 다시 받았을 때 플레이어를 새로 채우려고 올린다 */
    val playbackVersion: Int = 0,
    val isRequesting: Boolean = false,
    val isSaving: Boolean = false,
    @param:StringRes val message: Int? = null,
    val messageIsError: Boolean = false,
    val deleteTarget: DayClip? = null,
    /** Android 8~9 저장 권한을 화면이 요청해야 함 */
    val needsStoragePermission: Boolean = false,
    /** 화면이 띄울 공유 시트 */
    val shareIntent: Intent? = null,
) {
    val status: DayVlogStatus get() = day?.vlog?.status ?: DayVlogStatus.NONE

    /** 내 달력의 오늘 화면에서만 만들 수 있다 */
    val canMakeVlog: Boolean get() = !readOnly && isToday

    /** 완성된 브이로그가 있을 때만 저장·공유 (친구 것은 안 보인다) */
    val canSave: Boolean
        get() = !readOnly && day?.vlog?.let { it.status == DayVlogStatus.DONE && it.videoUrl != null } == true

    val slots: List<TimelineSlot>
        get() {
            val day = day ?: return emptyList()
            val start = minOf(day.notifyStartHour, day.notifyEndHour)
            val end = maxOf(day.notifyStartHour, day.notifyEndHour)
            // 알림 시간대 밖에 찍힌 클립(설정을 바꾼 경우)도 빠지지 않게 합친다
            val hours = ((start..end) + day.clips.map { it.slotHour }).toSortedSet()
            return hours.map { hour -> TimelineSlot(hour, day.clips.firstOrNull { it.slotHour == hour }) }
        }

    val playback: Playback?
        get() {
            val day = day ?: return null
            val clipIds = day.clips.joinToString(",") { it.id.toString() }
            day.clips.firstOrNull { it.id == selectedClipId }?.let { clip ->
                return Playback("clip:${clip.id}:$playbackVersion", listOf(clip.videoUrl), clip.thumbnailUrl, autoPlay = true)
            }
            val vlog = day.vlog
            val vlogUrl = vlog.videoUrl
            if (vlog.status == DayVlogStatus.DONE && vlogUrl != null) {
                return Playback("vlog:$clipIds:$playbackVersion", listOf(vlogUrl), vlog.thumbnailUrl, autoPlay = false)
            }
            if (day.clips.isEmpty()) return null
            return Playback(
                key = "clips:$clipIds:$playbackVersion",
                urls = day.clips.map { it.videoUrl },
                thumbnailUrl = day.clips.first().thumbnailUrl,
                autoPlay = false,
            )
        }
}

@HiltViewModel
internal class DayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dayRepository: DayRepository,
    private val vlogRepository: VlogRepository,
    private val vlogSaver: VlogSaver,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<DayRoute>()
    private val date = LocalDate.parse(route.date)

    private val _uiState = MutableStateFlow(
        DayUiState(
            date = date,
            readOnly = route.readOnly,
            friendNickname = route.friendNickname,
            isToday = date == todayInKorea(),
        ),
    )
    val uiState: StateFlow<DayUiState> = _uiState.asStateFlow()

    private var pollJob: Job? = null
    private var isVisible = false
    private var retriedPlayback = false

    /** 화면이 보이기 시작할 때 (ON_RESUME). 주소가 만료됐을 수 있어 매번 새로 받는다 */
    fun onVisible() {
        isVisible = true
        retriedPlayback = false
        load()
    }

    /** 화면이 가려질 때 (ON_PAUSE). 만드는 중 재조회를 멈춘다 */
    fun onHidden() {
        isVisible = false
        pollJob?.cancel()
    }

    fun retry() = load()

    private fun load(onLoaded: (() -> Unit)? = null) {
        viewModelScope.launch {
            val before = _uiState.value.status
            when (val result = dayRepository.getDay(route.userId, date)) {
                is DayResult.Success -> {
                    val day = result.day
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            loadError = null,
                            day = day,
                            // 지워진 클립을 고르고 있었으면 풀어 준다
                            selectedClipId = state.selectedClipId?.takeIf { id -> day.clips.any { it.id == id } },
                        )
                    }
                    if (before.isGenerating && day.vlog.status == DayVlogStatus.DONE) {
                        showMessage(R.string.day_vlog_done_now, isError = false)
                    }
                    schedulePollIfGenerating()
                    onLoaded?.invoke()
                }
                is DayResult.Failure -> _uiState.update {
                    it.copy(isLoading = false, loadError = result.error.messageRes())
                }
            }
        }
    }

    /** 생성 중이면 몇 초 뒤 다시 조회 → 완성되면 playback 이 브이로그로 바뀌어 플레이어가 자동으로 넘어간다 */
    private fun schedulePollIfGenerating() {
        pollJob?.cancel()
        if (!isVisible || !_uiState.value.status.isGenerating) return
        pollJob = viewModelScope.launch {
            delay(POLL_INTERVAL_MS)
            load()
        }
    }

    /** 타임라인 칸을 누름: 그 클립만 재생. 같은 칸을 다시 누르면 전체로 돌아간다 */
    fun onSlotClick(slot: TimelineSlot) {
        val clip = slot.clip ?: return
        _uiState.update { it.copy(selectedClipId = if (it.selectedClipId == clip.id) null else clip.id) }
    }

    /** 재생 실패. 대부분 주소(1시간 유효) 만료라 한 번만 새로 받아 다시 채운다 */
    fun onPlaybackError() {
        if (retriedPlayback) {
            showMessage(R.string.day_play_error, isError = true)
            return
        }
        retriedPlayback = true
        load { _uiState.update { it.copy(playbackVersion = it.playbackVersion + 1) } }
    }

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
                it.copy(isRequesting = false, message = message, messageIsError = result is VlogRequestResult.Failure)
            }
            // 상태 배지를 "만드는 중"으로 바꾸고 완성될 때까지 재조회
            if (result == VlogRequestResult.Started) load()
        }
    }

    fun askDelete(clip: DayClip) {
        if (_uiState.value.readOnly) return
        _uiState.update { it.copy(deleteTarget = clip) }
    }

    fun dismissDelete() = _uiState.update { it.copy(deleteTarget = null) }

    fun confirmDelete() {
        val clip = _uiState.value.deleteTarget ?: return
        _uiState.update { it.copy(deleteTarget = null, message = null) }
        viewModelScope.launch {
            when (val error = dayRepository.deleteClip(clip.id)) {
                null -> {
                    showMessage(R.string.day_delete_done, isError = false)
                    load()
                }
                else -> showMessage(error.messageRes(), isError = true)
            }
        }
    }

    fun saveToPhone() = save(share = false)

    fun share() = save(share = true)

    fun onStoragePermissionResult(granted: Boolean, share: Boolean) {
        _uiState.update { it.copy(needsStoragePermission = false) }
        if (granted) save(share) else showMessage(R.string.day_save_error_permission, isError = true)
    }

    fun onShareShown() = _uiState.update { it.copy(shareIntent = null) }

    /** 마지막으로 요청한 게 공유였는지 (권한 요청 뒤 이어서 하려고) */
    var pendingShare: Boolean = false
        private set

    private fun save(share: Boolean, retried: Boolean = false) {
        val state = _uiState.value
        val url = state.day?.vlog?.videoUrl
        if (!state.canSave || url == null) {
            showMessage(R.string.day_save_need_vlog, isError = true)
            return
        }
        if (state.isSaving && !retried) return
        pendingShare = share
        _uiState.update { it.copy(isSaving = true, message = null) }
        viewModelScope.launch {
            when (val result = vlogSaver.saveToGallery(url, fileName = "setmon_$date")) {
                is VlogSaveResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    if (share) {
                        _uiState.update { it.copy(shareIntent = vlogSaver.shareIntent(result.uri)) }
                    } else {
                        showMessage(R.string.day_saved, isError = false)
                    }
                }
                is VlogSaveResult.Failure -> when (result.error) {
                    // 주소(1시간 유효)가 만료됨 → 새로 받아 한 번만 다시
                    VlogSaveError.URL_EXPIRED -> if (retried) {
                        failSave(R.string.day_vlog_error_unknown)
                    } else {
                        load { save(share, retried = true) }
                    }
                    VlogSaveError.PERMISSION_REQUIRED ->
                        _uiState.update { it.copy(isSaving = false, needsStoragePermission = true) }
                    VlogSaveError.NETWORK -> failSave(R.string.day_vlog_error_network)
                    VlogSaveError.STORAGE -> failSave(R.string.day_save_error_storage)
                }
            }
        }
    }

    private fun failSave(@StringRes message: Int) {
        _uiState.update { it.copy(isSaving = false) }
        showMessage(message, isError = true)
    }

    private fun showMessage(@StringRes message: Int, isError: Boolean) =
        _uiState.update { it.copy(message = message, messageIsError = isError) }

    private fun VlogError.messageRes(): Int = when (this) {
        VlogError.NO_CLIPS -> R.string.day_vlog_error_no_clips
        VlogError.NETWORK -> R.string.day_vlog_error_network
        VlogError.UNKNOWN -> R.string.day_vlog_error_unknown
    }

    private fun DayError.messageRes(): Int = when (this) {
        DayError.NETWORK -> R.string.day_vlog_error_network
        DayError.FORBIDDEN -> R.string.day_error_forbidden
        DayError.UNKNOWN -> R.string.day_vlog_error_unknown
    }
}
