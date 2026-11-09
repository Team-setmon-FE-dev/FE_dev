package com.setmon.feature.capture.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.setmon.feature.capture.CaptureScreen
import com.setmon.feature.capture.CaptureViewModel
import com.setmon.feature.capture.data.dto.ClipRegisterResponse

const val CAPTURE_ROUTE = "capture_route"

fun NavController.navigateToCapture(navOptions: NavOptions? = null) {
    this.navigate(CAPTURE_ROUTE, navOptions)
}

fun NavGraphBuilder.captureScreen(
    onUploadSuccess: (ClipRegisterResponse) -> Unit,
    onBack: () -> Unit
) {
    composable(route = CAPTURE_ROUTE) {
        // @Composable 영역인 composable 람다 내부에서 ViewModel을 주입받습니다.
        val viewModel: CaptureViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        CaptureScreen(
            uiState = uiState,
            onRecordComplete = { videoFile, thumbFile, durationMs ->
                viewModel.uploadClip(videoFile, thumbFile, durationMs)
            },
            onUploadSuccess = onUploadSuccess,
            onBack = onBack
        )
    }
}