package com.setmon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.setmon.core.designsystem.theme.SetmonTheme

/*
 * 피그마에 그리지 않은 공통 화면 상태를 Android Studio 미리보기로 확인하는 파일.
 * 각 미리보기를 우클릭 → Copy Image 하면 피그마에 그림으로 붙여넣을 수 있다.
 * 미리보기 크기는 피그마 프레임과 같은 412dp 폭.
 */

@Preview(name = "1. Loading", widthDp = 412, heightDp = 300, showBackground = true, backgroundColor = 0xFFF5F0E6)
@Composable
private fun LoadingPreview() {
    SetmonTheme { LoadingView() }
}

@Preview(name = "2. Error", widthDp = 412, heightDp = 300, showBackground = true, backgroundColor = 0xFFF5F0E6)
@Composable
private fun ErrorPreview() {
    SetmonTheme {
        ErrorView(message = "불러오지 못했어요.\n잠시 후 다시 시도해 주세요.", onRetry = {})
    }
}

@Preview(name = "3. Empty", widthDp = 412, heightDp = 300, showBackground = true, backgroundColor = 0xFFF5F0E6)
@Composable
private fun EmptyPreview() {
    SetmonTheme {
        EmptyView(message = "아직 친구가 없어요.\n친구 코드로 요청을 보내 보세요.") {
            SetmonButton(text = "친구 추가", onClick = {})
        }
    }
}

@Preview(name = "4. Toast (Snackbar)", widthDp = 412, heightDp = 120, showBackground = true, backgroundColor = 0xFFF5F0E6)
@Composable
private fun SnackbarPreview() {
    SetmonTheme {
        // SetmonSnackbarHost 와 같은 색·모양
        Box(Modifier.padding(16.dp)) {
            Snackbar(
                shape = SetmonTheme.shapes.button,
                containerColor = SetmonTheme.colors.textPrimary,
                contentColor = SetmonTheme.colors.surface,
                action = {
                    TextButton(onClick = {}) {
                        Text("되돌리기", color = SetmonTheme.colors.accent, style = SetmonTheme.typography.label)
                    }
                },
            ) {
                Text("친구 코드를 복사했어요", style = SetmonTheme.typography.body)
            }
        }
    }
}

@Preview(name = "5. ConfirmDialog", widthDp = 412, heightDp = 400, showBackground = true, backgroundColor = 0xFFF5F0E6)
@Composable
private fun ConfirmDialogPreview() {
    SetmonTheme {
        Box(Modifier.background(SetmonTheme.colors.background)) {
            ConfirmDialog(
                title = "클립을 삭제할까요?",
                message = "14:00 클립이 지워지고, 그날 브이로그가 다시 만들어져요.",
                confirmLabel = "삭제",
                onConfirm = {},
                onDismiss = {},
                destructive = true,
            )
        }
    }
}
