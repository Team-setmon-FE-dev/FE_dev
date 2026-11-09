package com.setmon.feature.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setmon.feature.capture.data.ClipUploader
import com.setmon.feature.capture.data.dto.ClipRegisterResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed interface CaptureUiState {
    object Idle : CaptureUiState
    object Uploading : CaptureUiState
    data class Success(val response: ClipRegisterResponse) : CaptureUiState
    data class Error(val message: String) : CaptureUiState
}

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val clipUploader: ClipUploader
) : ViewModel() {

    private val _uiState = MutableStateFlow<CaptureUiState>(CaptureUiState.Idle)
    val uiState: StateFlow<CaptureUiState> = _uiState

    fun uploadClip(videoFile: File, thumbFile: File, durationMs: Long) {
        viewModelScope.launch {
            _uiState.value = CaptureUiState.Uploading
            val result = clipUploader.uploadClipProcess(
                videoFile = videoFile,
                thumbFile = thumbFile,
                durationMs = durationMs
            )
            result.onSuccess { response ->
                _uiState.value = CaptureUiState.Success(response)
            }.onFailure { exception ->
                _uiState.value = CaptureUiState.Error(exception.message ?: "알 수 없는 오류가 발생했습니다.")
            }
        }
    }

    fun resetState() {
        _uiState.value = CaptureUiState.Idle
    }
}