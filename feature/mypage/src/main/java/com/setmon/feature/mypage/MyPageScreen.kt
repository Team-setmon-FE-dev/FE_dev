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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.setmon.core.designsystem.component.ErrorView
import com.setmon.core.designsystem.component.LoadingView
import com.setmon.core.designsystem.component.SetmonButton
import com.setmon.core.designsystem.component.SetmonButtonType
import com.setmon.core.designsystem.component.SetmonTopBar
import com.setmon.core.designsystem.theme.SetmonTheme
import com.setmon.core.model.MyProfile

/** S-12 마이페이지 기본 화면. 피그마 `S-12 · My page`. 알림 설정·탈퇴는 3주차 */
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
        onBack = onBack,
    )
}

@Composable
private fun MyPageScreen(
    uiState: MyPageUiState,
    onRetry: () -> Unit,
    onLogoutClick: () -> Unit,
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
                isLoggingOut = uiState.isLoggingOut,
                logoutFailed = uiState.logoutFailed,
                onLogoutClick = onLogoutClick,
            )
        }
    }
}

@Composable
private fun MyPageBody(
    profile: MyProfile,
    isLoggingOut: Boolean,
    logoutFailed: Boolean,
    onLogoutClick: () -> Unit,
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
            text = stringResource(R.string.mypage_account),
            style = typography.subtitle,
            color = colors.textPrimary,
        )
        SetmonButton(
            text = stringResource(R.string.mypage_logout),
            onClick = onLogoutClick,
            type = SetmonButtonType.Text,
            loading = isLoggingOut,
        )
        if (logoutFailed) {
            Text(
                text = stringResource(R.string.mypage_logout_failed),
                style = typography.label,
                color = colors.error,
                modifier = Modifier.padding(start = 20.dp),
            )
        }
    }
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
            onBack = {},
        )
    }
}
