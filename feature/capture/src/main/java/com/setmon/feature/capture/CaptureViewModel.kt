package com.setmon.feature.capture

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setmon.feature.capture.data.ClipUploader
import com.setmon.feature.capture.data.dto.ClipRegisterResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

// ==========================================
// Capture UI State (단일 선언)
// ==========================================
sealed interface CaptureUiState {
    object Idle : CaptureUiState
    data class Uploading(val retryAttempt: Int = 0) : CaptureUiState
    data class UploadFailed(
        val videoFile: File,
        val thumbFile: File,
        val durationMs: Long,
        val errorMessage: String
    ) : CaptureUiState
    data class Success(val response: ClipRegisterResponse) : CaptureUiState
    data class Error(val message: String) : CaptureUiState
}

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val clipUploader: ClipUploader
) : ViewModel() {

    private val _uiState = MutableStateFlow<CaptureUiState>(CaptureUiState.Idle)
    val uiState: StateFlow<CaptureUiState> = _uiState

    /**
     * 클립 업로드 처리 (최대 3회 자동 재시도 적용)
     * 1초, 2초 간격으로 지연 후 재시도하며, 3회 모두 실패 시 UploadFailed 상태로 전환
     */
    fun uploadClipWithRetry(videoFile: File, thumbFile: File, durationMs: Long) {
        viewModelScope.launch {
            val maxRetries = 3
            var lastException: Throwable? = null

            for (attempt in 1..maxRetries) {
                _uiState.value = CaptureUiState.Uploading(retryAttempt = attempt - 1)

                val result = clipUploader.uploadClipProcess(
                    videoFile = videoFile,
                    thumbFile = thumbFile,
                    durationMs = durationMs
                )

                if (result.isSuccess) {
                    val response = result.getOrThrow()
                    _uiState.value = CaptureUiState.Success(response)
                    return@launch
                } else {
                    lastException = result.exceptionOrNull()
                    Log.w("CaptureViewModel", "Upload attempt $attempt failed: ${lastException?.message}")
                    if (attempt < maxRetries) {
                        delay(attempt * 1000L) // 1초, 2초 지연 후 재시도
                    }
                }
            }

            // 3회 실패 시 수동 재시도 버튼 UI(UploadFailed) 상태로 전환
            _uiState.value = CaptureUiState.UploadFailed(
                videoFile = videoFile,
                thumbFile = thumbFile,
                durationMs = durationMs,
                errorMessage = lastException?.message ?: "네트워크 연결이 불안정합니다."
            )
        }
    }

    /**
     * 이전 단일 업로드 메서드 (호환성 유지용)
     */
    fun uploadClip(videoFile: File, thumbFile: File, durationMs: Long) {
        uploadClipWithRetry(videoFile, thumbFile, durationMs)
    }

    fun resetState() {
        _uiState.value = CaptureUiState.Idle
    }
}