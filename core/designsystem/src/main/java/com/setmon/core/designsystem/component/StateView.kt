package com.setmon.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.setmon.core.designsystem.theme.SetmonTheme

/* 로딩·에러·비어 있음 화면, 토스트, 확인창. 피그마에는 따로 그리지 않고 코드로만 둔다. */

@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = SetmonTheme.colors.primary)
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryLabel: String = "다시 시도",
) {
    MessageView(message = message, modifier = modifier) {
        SetmonButton(text = retryLabel, onClick = onRetry, type = SetmonButtonType.Secondary)
    }
}

@Composable
fun EmptyView(
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    MessageView(message = message, modifier = modifier, action = action)
}

@Composable
private fun MessageView(
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = SetmonTheme.typography.body,
            color = SetmonTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            Box(Modifier.height(16.dp))
            action()
        }
    }
}

/** 토스트 대신 쓰는 하단 알림. Scaffold(snackbarHost = { SetmonSnackbarHost(state) }) 로 붙인다. */
@Composable
fun SetmonSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        Snackbar(
            snackbarData = data,
            shape = SetmonTheme.shapes.button,
            containerColor = SetmonTheme.colors.textPrimary,
            contentColor = SetmonTheme.colors.surface,
            actionColor = SetmonTheme.colors.accent,
        )
    }
}

/** 삭제·탈퇴·로그아웃 확인창. [destructive] 면 확인 글자를 error 색으로. */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissLabel: String = "취소",
    destructive: Boolean = false,
) {
    val colors = SetmonTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = SetmonTheme.shapes.card,
        title = { Text(title, style = SetmonTheme.typography.subtitle, color = colors.textPrimary) },
        text = { Text(message, style = SetmonTheme.typography.body, color = colors.textSecondary) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, style = SetmonTheme.typography.label, color = if (destructive) colors.error else colors.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissLabel, style = SetmonTheme.typography.label, color = colors.textSecondary)
            }
        },
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E6, heightDp = 240)
@Composable
private fun ErrorViewPreview() {
    SetmonTheme {
        ErrorView(message = "불러오지 못했어요.\n잠시 후 다시 시도해 주세요.", onRetry = {}, modifier = Modifier.fillMaxWidth())
    }
}
