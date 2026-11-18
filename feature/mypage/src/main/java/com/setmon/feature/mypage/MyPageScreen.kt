package com.setmon.feature.mypage

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.setmon.core.designsystem.component.ConfirmDialog
import com.setmon.core.designsystem.component.ErrorView
import com.setmon.core.designsystem.component.LoadingView
import com.setmon.core.designsystem.component.SetmonButton
import com.setmon.core.designsystem.component.SetmonButtonType
import com.setmon.core.designsystem.component.SetmonTopBar
import com.setmon.core.designsystem.theme.SetmonTheme
import com.setmon.core.model.MyProfile
import com.setmon.core.model.NotificationSettings

/** S-12 마이페이지. 피그마 `S-12 · My page` / `(delete account)` */
@Composable
internal fun MyPageRoot(
    onLogout: () -> Unit,
    onBack: () -> Unit,
    viewModel: MyPageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.loggedOut) {
        if (uiState.loggedOut) onLogout()
    }

    MyPageScreen(
        uiState = uiState,
        onRetry = viewModel::load,
        onLogoutClick = viewModel::logout,
        onSettingsChange = viewModel::updateSettings,
        onWithdrawClick = viewModel::askWithdraw,
        onBack = onBack,
    )

    if (uiState.showWithdrawDialog) {
        ConfirmDialog(
            title = stringResource(R.string.mypage_withdraw_title),
            message = stringResource(R.string.mypage_withdraw_message),
            confirmLabel = stringResource(R.string.mypage_withdraw_confirm),
            dismissLabel = stringResource(R.string.mypage_withdraw_cancel),
            onConfirm = viewModel::withdraw,
            onDismiss = viewModel::dismissWithdraw,
            destructive = true,
        )
    }
}

@Composable
private fun MyPageScreen(
    uiState: MyPageUiState,
    onRetry: () -> Unit,
    onLogoutClick: () -> Unit,
    onSettingsChange: (NotificationSettings) -> Unit,
    onWithdrawClick: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SetmonTheme.colors.background)
            .systemBarsPadding(),
    ) {
        SetmonTopBar(title = stringResource(R.string.mypage_title), onBack = onBack)
        when (val content = uiState.content) {
            MyPageContent.Loading -> LoadingView()
            is MyPageContent.Error -> ErrorView(
                message = stringResource(content.message),
                onRetry = onRetry,
                retryLabel = stringResource(R.string.mypage_retry),
            )
            is MyPageContent.Loaded -> MyPageBody(
                profile = content.profile,
                uiState = uiState,
                onLogoutClick = onLogoutClick,
                onSettingsChange = onSettingsChange,
                onWithdrawClick = onWithdrawClick,
            )
        }
    }
}

@Composable
private fun MyPageBody(
    profile: MyProfile,
    uiState: MyPageUiState,
    onLogoutClick: () -> Unit,
    onSettingsChange: (NotificationSettings) -> Unit,
    onWithdrawClick: () -> Unit,
) {
    val colors = SetmonTheme.colors
    val typography = SetmonTheme.typography
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = SetmonTheme.spacing.screenHorizontal, vertical = 16.dp),
    ) {
        ProfileHeader(profile)
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            StatCard(label = stringResource(R.string.mypage_days_recorded), modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.totalRecordDays.toString(),
                    style = typography.display,
                    color = colors.primary,
                )
            }
            StatCard(label = stringResource(R.string.mypage_friend_code), modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.friendCode,
                    style = typography.title.copy(letterSpacing = 2.sp),
                    color = colors.textPrimary,
                )
                CopyButton(friendCode = profile.friendCode)
            }
        }
        Spacer(Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.mypage_reminders),
            style = typography.subtitle,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(8.dp))
        RemindersCard(settings = profile.settings, onChange = onSettingsChange)
        uiState.settingsError?.let { error ->
            Text(
                text = stringResource(error),
                style = typography.label,
                color = colors.error,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp),
            )
        }

        Spacer(Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.mypage_account),
            style = typography.subtitle,
            color = colors.textPrimary,
        )
        SetmonButton(
            text = stringResource(R.string.mypage_logout),
            onClick = onLogoutClick,
            type = SetmonButtonType.Text,
            loading = uiState.isLoggingOut,
        )
        if (uiState.logoutFailed) {
            Text(
                text = stringResource(R.string.mypage_logout_failed),
                style = typography.label,
                color = colors.error,
                modifier = Modifier.padding(start = 20.dp),
            )
        }
        // 피그마처럼 빨간 글자. SetmonButton Text 형은 초록이라 직접 그린다
        TextButton(onClick = onWithdrawClick, enabled = !uiState.isWithdrawing) {
            Text(
                text = stringResource(R.string.mypage_withdraw),
                style = typography.label,
                color = colors.error,
            )
        }
        if (uiState.withdrawFailed) {
            Text(
                text = stringResource(R.string.mypage_withdraw_failed),
                style = typography.label,
                color = colors.error,
                modifier = Modifier.padding(start = 20.dp),
            )
        }
    }
}

/** 피그마 Reminders 카드: 시작·끝 시각, 촬영 알림, 완성 알림. 바꾸면 바로 저장 */
@Composable
private fun RemindersCard(settings: NotificationSettings, onChange: (NotificationSettings) -> Unit) {
    val colors = SetmonTheme.colors
    val shape = SetmonTheme.shapes.card
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, shape)
            .border(1.dp, colors.outline, shape)
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        SettingRow(label = stringResource(R.string.mypage_reminders_start)) {
            HourChip(hour = settings.notifyStartHour) { onChange(settings.copy(notifyStartHour = it)) }
        }
        SettingRow(label = stringResource(R.string.mypage_reminders_end)) {
            HourChip(hour = settings.notifyEndHour) { onChange(settings.copy(notifyEndHour = it)) }
        }
        SettingRow(label = stringResource(R.string.mypage_capture_notify)) {
            SetmonSwitch(checked = settings.captureNotifyOn) { onChange(settings.copy(captureNotifyOn = it)) }
        }
        SettingRow(label = stringResource(R.string.mypage_complete_notify)) {
            SetmonSwitch(checked = settings.completeNotifyOn) { onChange(settings.copy(completeNotifyOn = it)) }
        }
    }
}

@Composable
private fun SettingRow(label: String, control: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = SetmonTheme.typography.body,
            color = SetmonTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        control()
    }
}

/** "09:00" 칸. 누르면 0~23시 목록 */
@Composable
private fun HourChip(hour: Int, onSelect: (Int) -> Unit) {
    val colors = SetmonTheme.colors
    val shape = SetmonTheme.shapes.thumbnail
    var expanded by remember { mutableStateOf(false) }
    Box {
        Text(
            text = stringResource(R.string.mypage_hour, hour),
            style = SetmonTheme.typography.body,
            color = colors.textPrimary,
            modifier = Modifier
                .clip(shape)
                .background(colors.background, shape)
                .border(1.dp, colors.outline, shape)
                .clickable(role = Role.DropdownList) { expanded = true }
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 320.dp),
        ) {
            (0..23).forEach { h ->
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.mypage_hour, h), style = SetmonTheme.typography.body) },
                    onClick = {
                        expanded = false
                        if (h != hour) onSelect(h)
                    },
                )
            }
        }
    }
}

@Composable
private fun SetmonSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colors = SetmonTheme.colors
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedTrackColor = colors.primary,
            checkedThumbColor = colors.onPrimary,
            uncheckedTrackColor = colors.outline,
            uncheckedThumbColor = colors.surface,
            uncheckedBorderColor = colors.outline,
        ),
    )
}

@Composable
private fun ProfileHeader(profile: MyProfile) {
    val colors = SetmonTheme.colors
    val typography = SetmonTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(colors.accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = profile.nickname.initial(), style = typography.title, color = colors.textPrimary)
        }
        Column {
            Text(text = profile.nickname, style = typography.title, color = colors.textPrimary)
            Text(text = profile.email, style = typography.label, color = colors.textSecondary)
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = SetmonTheme.colors
    val shape = SetmonTheme.shapes.card
    Column(
        modifier = modifier
            .background(colors.surface, shape)
            .border(1.dp, colors.outline, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = label, style = SetmonTheme.typography.label, color = colors.textSecondary)
        content()
    }
}

@Composable
private fun CopyButton(friendCode: String) {
    val context = LocalContext.current
    var copied by remember(friendCode) { mutableStateOf(false) }
    SetmonButton(
        text = stringResource(if (copied) R.string.mypage_copied else R.string.mypage_copy),
        onClick = {
            context.getSystemService(ClipboardManager::class.java)
                .setPrimaryClip(ClipData.newPlainText("friend code", friendCode))
            copied = true
        },
        type = SetmonButtonType.Text,
    )
}

/** 이모지처럼 두 글자로 저장되는 문자도 반으로 자르지 않는다 */
private fun String.initial(): String =
    if (isEmpty()) "" else substring(0, offsetByCodePoints(0, 1)).uppercase()

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E6, heightDp = 640)
@Composable
private fun MyPageScreenPreview() {
    SetmonTheme {
        MyPageScreen(
            uiState = MyPageUiState(
                content = MyPageContent.Loaded(
                    MyProfile(1L, "yeeun@setmon.app", "Yeeun", "K7Q2MX", 12),
                ),
            ),
            onRetry = {},
            onLogoutClick = {},
            onSettingsChange = {},
            onWithdrawClick = {},
            onBack = {},
        )
    }
}
