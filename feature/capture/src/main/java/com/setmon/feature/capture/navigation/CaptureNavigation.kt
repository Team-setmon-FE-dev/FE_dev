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
import kotlinx.serialization.Serializable

/** S-04 촬영 — route: capture */
@Serializable
data object CaptureRoute

fun NavController.navigateToCapture(navOptions: NavOptions? = null) =
    navigate(CaptureRoute, navOptions)

/**
 * A ↔ B 계약 (docs/MODULES.md 5번). 이름과 파라미터를 바꾸려면 A와 먼저 합의한다.
 *
 * @param onUploadComplete 업로드 성공 시 호출 → A가 달력으로 돌아가 새로고침
 * @param onBack 촬영을 그만두고 나갈 때 호출
 */
fun NavGraphBuilder.captureScreen(
    onUploadComplete: () -> Unit,
    onBack: () -> Unit,
) {
    composable<CaptureRoute> {
        val viewModel: CaptureViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        val isSlotOccupied by viewModel.isSlotOccupied.collectAsStateWithLifecycle()

        CaptureScreen(
            uiState = uiState,
            isSlotOccupied = isSlotOccupied,
            onUpload = viewModel::uploadClip,
            onDismissError = viewModel::resetState,
            onUploadComplete = onUploadComplete,
            onBack = onBack,
        )
    }
}
