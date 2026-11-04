package com.setmon.feature.capture.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.setmon.feature.capture.CaptureScreen
import kotlinx.serialization.Serializable

/** S-04 촬영 — route: capture (B 담당) */
@Serializable
data object CaptureRoute

fun NavController.navigateToCapture(navOptions: NavOptions? = null) =
    navigate(CaptureRoute, navOptions)

/**
 * ★ A ↔ B 계약 ★
 * app 모듈의 NavHost는 이 함수 모양만 보고 촬영 화면을 연결한다.
 * B는 CaptureScreen 내부를 자유롭게 만들되, 이 함수의 이름과 파라미터는 바꾸기 전에 A와 합의한다.
 *
 * @param onUploadComplete 업로드가 끝났을 때 B가 호출 → A가 달력으로 돌아가 새로고침
 * @param onBack 사용자가 촬영을 그만두고 나갈 때 B가 호출
 */
fun NavGraphBuilder.captureScreen(
    onUploadComplete: () -> Unit,
    onBack: () -> Unit,
) {
    composable<CaptureRoute> {
        CaptureScreen(onUploadComplete = onUploadComplete, onBack = onBack)
    }
}
