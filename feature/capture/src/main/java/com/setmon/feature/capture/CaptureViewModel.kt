package com.setmon.feature.capture

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setmon.core.data.auth.AuthRepository
import com.setmon.feature.capture.data.ClipRegisterResponse
import com.setmon.feature.capture.data.ClipUploadException
import com.setmon.feature.capture.data.ClipUploader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

/** 올릴 클립 한 개. 재시도 버튼이 같은 값으로 다시 올릴 수 있게 묶어 둔다 */
data class PendingClip(
    val videoFile: File,
    val thumbFile: File,
    /** 실제 영상 길이. 서버 규격 1000~2000ms */
    val durationMs: Int,
    /** 녹화를 시작한 시각 */
    val recordedAt: Instant,
)

sealed interface CaptureUiState {
    data object Idle : CaptureUiState

    /** @param attempt 몇 번째 시도인지 (1부터) */
    data class Uploading(val attempt: Int = 1) : CaptureUiState

    /**
     * @param errorCode 명세서의 에러 코드. 서버가 답하지 못했으면 null
     * @param canRetry false 면 같은 클립을 다시 올려도 실패한다 (PAST_SLOT 등) → 다시 찍어야 한다
     */
    data class UploadFailed(
        val clip: PendingClip,
        val errorCode: String?,
        val errorMessage: String,
        val canRetry: Boolean,
    ) : CaptureUiState

    data class Success(val response: ClipRegisterResponse) : CaptureUiState
}

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val clipUploader: ClipUploader,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CaptureUiState>(CaptureUiState.Idle)
    val uiState: StateFlow<CaptureUiState> = _uiState

    /** 이번 시간(KST)에 이미 올린 클립이 있으면 true → "올리면 교체됩니다" 안내 */
    private val _isSlotOccupied = MutableStateFlow(false)
    val isSlotOccupied: StateFlow<Boolean> = _isSlotOccupied

    init {
        refreshSlotOccupied()
    }

    private fun refreshSlotOccupied() {
        viewModelScope.launch {
            val userId = authRepository.savedUserId() ?: return@launch
            val now = ZonedDateTime.now(KST)
            val occupied = clipUploader.isSlotOccupied(userId, now.toLocalDate(), now.hour)
            if (occupied != null) _isSlotOccupied.value = occupied
        }
    }

    /** 자동 3회 재시도는 [ClipUploader] 안에서 한다. 그래도 실패하면 UploadFailed 로 재시도 버튼을 띄운다 */
    fun uploadClip(clip: PendingClip) {
        if (_uiState.value is CaptureUiState.Uploading) return
        _uiState.value = CaptureUiState.Uploading()
        viewModelScope.launch {
            val result = clipUploader.upload(
                videoFile = clip.videoFile,
                thumbFile = clip.thumbFile,
                durationMs = clip.durationMs,
                recordedAt = clip.recordedAt,
                onAttempt = { attempt -> _uiState.value = CaptureUiState.Uploading(attempt) },
            )
            _uiState.value = result.fold(
                onSuccess = { response ->
                    clip.videoFile.delete()
                    clip.thumbFile.delete()
                    CaptureUiState.Success(response)
                },
                onFailure = { e ->
                    Log.w("CaptureViewModel", "Upload failed: ${e.message}", e)
                    val uploadError = e as? ClipUploadException
                    CaptureUiState.UploadFailed(
                        clip = clip,
                        errorCode = uploadError?.code,
                        errorMessage = e.message ?: "업로드에 실패했습니다.",
                        canRetry = uploadError?.retryable ?: true,
                    )
                },
            )
        }
    }

    fun resetState() {
        _uiState.value = CaptureUiState.Idle
    }

    private companion object {
        /** 시간 슬롯은 서버와 같이 KST 기준 */
        val KST: ZoneId = ZoneId.of("Asia/Seoul")
    }
}
