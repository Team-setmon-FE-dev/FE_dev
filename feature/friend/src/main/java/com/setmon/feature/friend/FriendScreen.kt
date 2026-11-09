package com.setmon.feature.friend

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.setmon.core.designsystem.component.ConfirmDialog
import com.setmon.core.designsystem.component.ErrorView
import com.setmon.core.designsystem.component.FriendListItem
import com.setmon.core.designsystem.component.LoadingView
import com.setmon.core.designsystem.component.SetmonButton
import com.setmon.core.designsystem.component.SetmonButtonType
import com.setmon.core.designsystem.component.SetmonTextField
import com.setmon.core.designsystem.component.SetmonTopBar
import com.setmon.core.designsystem.theme.SetmonTheme
import com.setmon.core.model.Friend
import com.setmon.core.model.FriendRequest

/** S-07 친구 (Mock). 피그마 `S-07 · Friends`. 서버 연결은 3주차 */
@Composable
internal fun FriendRoot(
    onFriendCalendarClick: (friendUserId: Long) -> Unit,
    onBack: () -> Unit,
    viewModel: FriendViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FriendScreen(
        uiState = uiState,
        onRetry = viewModel::load,
        onCodeChange = viewModel::onCodeChange,
        onSend = viewModel::sendRequest,
        onAccept = viewModel::accept,
        onDecline = viewModel::decline,
        onRemove = viewModel::askRemove,
        onFriendClick = { friend -> onFriendCalendarClick(friend.userId) },
        onBack = onBack,
    )

    uiState.removeTarget?.let { friend ->
        ConfirmDialog(
            title = stringResource(R.string.friend_remove_title, friend.nickname),
            message = stringResource(R.string.friend_remove_message),
            confirmLabel = stringResource(R.string.friend_remove),
            dismissLabel = stringResource(R.string.friend_cancel),
            onConfirm = viewModel::confirmRemove,
            onDismiss = viewModel::dismissRemove,
            destructive = true,
        )
    }
}

@Composable
private fun FriendScreen(
    uiState: FriendUiState,
    onRetry: () -> Unit,
    onCodeChange: (String) -> Unit,
    onSend: () -> Unit,
    onAccept: (FriendRequest) -> Unit,
    onDecline: (FriendRequest) -> Unit,
    onRemove: (Friend) -> Unit,
    onFriendClick: (Friend) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SetmonTheme.colors.background)
            .systemBarsPadding()
            .imePadding(),
    ) {
        SetmonTopBar(title = stringResource(R.string.friend_title), onBack = onBack)
        val loadError = uiState.loadError
        when {
            uiState.isLoading -> LoadingView()
            loadError != null -> ErrorView(
                message = stringResource(loadError),
                onRetry = onRetry,
                retryLabel = stringResource(R.string.friend_retry),
            )
            else -> FriendBody(uiState, onCodeChange, onSend, onAccept, onDecline, onRemove, onFriendClick)
        }
    }
}

@Composable
private fun FriendBody(
    uiState: FriendUiState,
    onCodeChange: (String) -> Unit,
    onSend: () -> Unit,
    onAccept: (FriendRequest) -> Unit,
    onDecline: (FriendRequest) -> Unit,
    onRemove: (Friend) -> Unit,
    onFriendClick: (Friend) -> Unit,
) {
    val colors = SetmonTheme.colors
    val typography = SetmonTheme.typography
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = SetmonTheme.spacing.screenHorizontal, vertical = 16.dp),
    ) {
        MyCodeCard(code = uiState.myCode)

        Spacer(Modifier.height(24.dp))
        SectionTitle(stringResource(R.string.friend_add))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            SetmonTextField(
                value = uiState.codeInput,
                onValueChange = onCodeChange,
                placeholder = stringResource(R.string.friend_code_hint),
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Send,
                ),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
            )
            SetmonButton(
                text = stringResource(R.string.friend_send),
                onClick = onSend,
                enabled = uiState.codeInput.isNotEmpty(),
                loading = uiState.isSending,
            )
        }
        uiState.message?.let { message ->
            Text(
                text = stringResource(message.text),
                style = typography.label,
                color = if (message.isError) colors.error else colors.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp),
            )
        }

        if (uiState.requests.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            SectionTitle(stringResource(R.string.friend_requests), count = uiState.requests.size)
            uiState.requests.forEach { request ->
                FriendListItem(
                    nickname = request.nickname,
                    subtitle = stringResource(R.string.friend_request_subtitle),
                    onAccept = { onAccept(request) },
                    onDecline = { onDecline(request) },
                    acceptLabel = stringResource(R.string.friend_accept),
                    declineLabel = stringResource(R.string.friend_decline),
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        SectionTitle(stringResource(R.string.friend_list), count = uiState.friends.size)
        if (uiState.friends.isEmpty()) {
            Text(
                text = stringResource(R.string.friend_empty),
                style = typography.body,
                color = colors.textSecondary,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        } else {
            uiState.friends.forEach { friend ->
                FriendListItem(
                    nickname = friend.nickname,
                    onClick = { onFriendClick(friend) },
                    onRemove = { onRemove(friend) },
                    removeLabel = stringResource(R.string.friend_remove),
                )
            }
            Text(
                text = stringResource(R.string.friend_open_hint),
                style = typography.caption,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun MyCodeCard(code: String) {
    val colors = SetmonTheme.colors
    val typography = SetmonTheme.typography
    val shape = SetmonTheme.shapes.card
    val context = LocalContext.current
    val shareText = stringResource(R.string.friend_share_text, code)
    var copied by remember(code) { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, shape)
            .border(1.dp, colors.outline, shape)
            .padding(start = 16.dp, top = 16.dp, end = 8.dp, bottom = 4.dp),
    ) {
        Text(
            text = stringResource(R.string.friend_my_code),
            style = typography.label,
            color = colors.textSecondary,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = code,
                style = typography.title.copy(letterSpacing = 2.sp),
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            SetmonButton(
                text = stringResource(if (copied) R.string.friend_copied else R.string.friend_copy),
                onClick = {
                    context.getSystemService(ClipboardManager::class.java)
                        .setPrimaryClip(ClipData.newPlainText("friend code", code))
                    copied = true
                },
                type = SetmonButtonType.Text,
            )
            SetmonButton(
                text = stringResource(R.string.friend_share),
                onClick = {
                    val send = Intent(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_TEXT, shareText)
                    context.startActivity(Intent.createChooser(send, null))
                },
                type = SetmonButtonType.Text,
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String, count: Int? = null) {
    Row(
        modifier = Modifier.padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, style = SetmonTheme.typography.subtitle, color = SetmonTheme.colors.textPrimary)
        if (count != null) {
            Text(count.toString(), style = SetmonTheme.typography.subtitle, color = SetmonTheme.colors.primary)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E6, heightDp = 760)
@Composable
private fun FriendScreenPreview() {
    SetmonTheme {
        FriendScreen(
            uiState = FriendUiState(
                isLoading = false,
                myCode = "K7Q2MX",
                requests = listOf(FriendRequest(1L, 4L, "Jisu")),
                friends = listOf(Friend(2L, "Minjun"), Friend(3L, "Taeyang")),
            ),
            onRetry = {},
            onCodeChange = {},
            onSend = {},
            onAccept = {},
            onDecline = {},
            onRemove = {},
            onFriendClick = {},
            onBack = {},
        )
    }
}
