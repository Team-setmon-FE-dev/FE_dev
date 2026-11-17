package com.setmon.feature.day

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.setmon.core.designsystem.component.ConfirmDialog
import com.setmon.core.designsystem.component.SetmonButton
import com.setmon.core.designsystem.component.SetmonButtonType
import com.setmon.core.designsystem.component.SetmonTopBar
import com.setmon.core.designsystem.theme.SetmonTheme
import com.setmon.core.model.DayClip
import com.setmon.core.model.DayVlogStatus

/** S-05 하루 상세. 피그마 `S-05 · Day` / `(generating)` / `(delete)` / `(read-only)` */
@Composable
internal fun DayRoot(
    onBack: () -> Unit,
    viewModel: DayViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onVisible() }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { viewModel.onHidden() }

    // Android 8~9 에서만 필요한 저장 권한 (B 의 VlogSaver 가 PERMISSION_REQUIRED 를 돌려줄 때)
    val storagePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.onStoragePermissionResult(granted, share = viewModel.pendingShare)
    }
    LaunchedEffect(uiState.needsStoragePermission) {
        if (uiState.needsStoragePermission) storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
    LaunchedEffect(uiState.shareIntent) {
        uiState.shareIntent?.let { intent ->
            context.startActivity(intent)
            viewModel.onShareShown()
        }
    }

    DayScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::retry,
        onSlotClick = viewModel::onSlotClick,
        onSlotLongClick = viewModel::askDelete,
        onPlaybackError = viewModel::onPlaybackError,
        onMakeVlog = viewModel::makeVlog,
        onSave = viewModel::saveToPhone,
        onShare = viewModel::share,
    )

    uiState.deleteTarget?.let { clip ->
        ConfirmDialog(
            title = stringResource(R.string.day_delete_title),
            message = stringResource(R.string.day_delete_message, clip.slotHour),
            confirmLabel = stringResource(R.string.day_delete_confirm),
            dismissLabel = stringResource(R.string.day_delete_cancel),
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::dismissDelete,
            destructive = true,
        )
    }
}

@Composable
private fun DayScreen(
    uiState: DayUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSlotClick: (TimelineSlot) -> Unit,
    onSlotLongClick: (DayClip) -> Unit,
    onPlaybackError: () -> Unit,
    onMakeVlog: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
) {
    val colors = SetmonTheme.colors
    val spacing = SetmonTheme.spacing
    val date = uiState.date
    val title = if (uiState.readOnly && uiState.friendNickname != null) {
        stringResource(R.string.day_title_friend, uiState.friendNickname, date.monthValue, date.dayOfMonth)
    } else {
        stringResource(R.string.day_title, date.monthValue, date.dayOfMonth)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding(),
    ) {
        SetmonTopBar(
            title = title,
            onBack = onBack,
            readOnly = uiState.readOnly,
            actionLabel = if (uiState.canSave) stringResource(R.string.day_share) else null,
            onAction = onShare,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenHorizontal),
        ) {
            Spacer(Modifier.height(spacing.xs))

            // 세로 영상(9:16)을 자르지 않고, 화면이 낮으면 폭을 줄여 가운데에 둔다
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                DayPlayer(
                    playback = uiState.playback,
                    status = uiState.status,
                    isLoading = uiState.isLoading,
                    onPlaybackError = onPlaybackError,
                )
            }

            uiState.loadError?.let { error ->
                Column(Modifier.fillMaxWidth().padding(top = spacing.sm), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(error), style = SetmonTheme.typography.label, color = colors.error)
                    SetmonButton(text = stringResource(R.string.day_retry), onClick = onRetry, type = SetmonButtonType.Text)
                }
            }

            if (uiState.status == DayVlogStatus.FAILED) {
                Text(
                    text = stringResource(R.string.day_vlog_failed),
                    style = SetmonTheme.typography.label,
                    color = colors.error,
                    modifier = Modifier.padding(top = spacing.xs),
                )
            }

            Text(
                text = stringResource(if (uiState.isToday) R.string.day_clips_today else R.string.day_clips),
                style = SetmonTheme.typography.subtitle,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = spacing.lg, bottom = spacing.xs),
            )
            Timeline(
                slots = uiState.slots,
                selectedClipId = uiState.selectedClipId,
                canDelete = !uiState.readOnly,
                onSlotClick = onSlotClick,
                onSlotLongClick = onSlotLongClick,
            )
            if (!uiState.readOnly && uiState.day?.clips?.isNotEmpty() == true) {
                Text(
                    text = stringResource(R.string.day_delete_hint),
                    style = SetmonTheme.typography.caption,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = spacing.xxs),
                )
            }

            Spacer(Modifier.height(spacing.lg))

            if (uiState.canMakeVlog) {
                SetmonButton(
                    text = stringResource(R.string.day_make_vlog),
                    onClick = onMakeVlog,
                    type = SetmonButtonType.Secondary,
                    loading = uiState.isRequesting,
                    enabled = !uiState.status.isGenerating,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (uiState.canSave) {
                SetmonButton(
                    text = stringResource(R.string.day_save_to_phone),
                    onClick = onSave,
                    type = SetmonButtonType.Text,
                    loading = uiState.isSaving,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = spacing.xs),
                )
            }
            uiState.message?.let { message ->
                Text(
                    text = stringResource(message),
                    style = SetmonTheme.typography.label,
                    color = if (uiState.messageIsError) colors.error else colors.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = spacing.xs),
                )
            }
            Spacer(Modifier.height(spacing.xl))
        }
    }
}

/** 알림 시간대만큼 시간 칸. 클립이 있으면 썸네일, 누르면 그 클립만 재생, 내 클립은 길게 눌러 삭제 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Timeline(
    slots: List<TimelineSlot>,
    selectedClipId: Long?,
    canDelete: Boolean,
    onSlotClick: (TimelineSlot) -> Unit,
    onSlotLongClick: (DayClip) -> Unit,
) {
    val colors = SetmonTheme.colors
    val shapes = SetmonTheme.shapes
    val listState = rememberLazyListState()
    // 처음 열 때 첫 클립(없으면 맨 앞) 칸이 보이게
    val firstClipIndex = slots.indexOfFirst { it.clip != null }
    LaunchedEffect(firstClipIndex >= 0) {
        if (firstClipIndex > 0) listState.scrollToItem((firstClipIndex - 1).coerceAtLeast(0))
    }

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(slots, key = { it.hour }) { slot ->
            val clip = slot.clip
            val selected = clip != null && clip.id == selectedClipId
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(width = 64.dp, height = 80.dp)
                        .clip(shapes.thumbnail)
                        .background(colors.surface)
                        .border(
                            width = if (selected) 1.5.dp else 1.dp,
                            color = if (selected) colors.primary else colors.outline,
                            shape = shapes.thumbnail,
                        )
                        .semantics { this.selected = selected }
                        .then(
                            if (clip != null) {
                                Modifier.combinedClickable(
                                    role = Role.Button,
                                    onClick = { onSlotClick(slot) },
                                    onLongClick = if (canDelete) ({ onSlotLongClick(clip) }) else null,
                                )
                            } else {
                                Modifier
                            },
                        ),
                ) {
                    if (clip != null) {
                        AsyncImage(
                            model = clip.thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            placeholder = ColorPainter(colors.outline),
                            error = ColorPainter(colors.outline),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(if (selected) 3.dp else 0.dp)
                                .clip(if (selected) shapes.thumbnailInner else shapes.thumbnail),
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.day_slot_hour, slot.hour),
                    style = SetmonTheme.typography.label,
                    color = if (clip != null) colors.textPrimary else colors.textSecondary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
    if (slots.all { it.clip == null } && slots.isNotEmpty()) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text(
                text = stringResource(R.string.day_no_clips),
                style = SetmonTheme.typography.label,
                color = colors.textSecondary,
            )
        }
    }
}
