package com.setmon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.setmon.core.designsystem.theme.SetmonTheme

/* ---------- ClipTimelineItem (S-05 시간대별 클립) ---------- */

enum class ClipSlotState { Filled, Empty, Playing }

/**
 * S-05 타임라인의 한 시간 칸 (64 × 100dp). 피그마 `ClipTimelineItem` (State=Filled/Empty/Playing).
 * 길게 누르기(삭제)는 화면 쪽에서 Modifier.combinedClickable 로 붙인다.
 */
@Composable
fun ClipTimelineItem(
    timeLabel: String,
    state: ClipSlotState,
    modifier: Modifier = Modifier,
    thumbnailUrl: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val colors = SetmonTheme.colors
    val shape = SetmonTheme.shapes.thumbnail
    Column(
        modifier = modifier.width(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 64.dp, height = 80.dp)
                .clip(shape)
                .then(
                    when (state) {
                        ClipSlotState.Playing -> Modifier.border(2.dp, colors.primary, shape)
                        ClipSlotState.Empty -> Modifier.border(1.dp, colors.outline, shape).background(colors.surface)
                        ClipSlotState.Filled -> Modifier
                    },
                )
                .then(if (onClick != null && state != ClipSlotState.Empty) Modifier.clickable(onClick = onClick) else Modifier),
        ) {
            if (state != ClipSlotState.Empty) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    colorFilter = SetmonThumbnailFilter,
                    placeholder = ColorPainter(colors.outline),
                    error = ColorPainter(colors.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(if (state == ClipSlotState.Playing) 3.dp else 0.dp)
                        .clip(SetmonTheme.shapes.thumbnailInner),
                )
            }
        }
        Text(
            text = timeLabel,
            style = SetmonTheme.typography.caption,
            color = if (state == ClipSlotState.Playing) colors.primary else colors.textSecondary,
        )
    }
}

/* ---------- FriendListItem (S-07) ---------- */

/**
 * 친구 목록·받은 요청 한 줄 (높이 64). 피그마 `FriendListItem` (Type=Friend/Request).
 * - Friend: [onRemove] 가 있으면 오른쪽에 Remove
 * - Request: [onAccept]·[onDecline] 이 있으면 Accept·Decline
 */
@Composable
fun FriendListItem(
    nickname: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null,
    onAccept: (() -> Unit)? = null,
    onDecline: (() -> Unit)? = null,
    removeLabel: String = "Remove",
    acceptLabel: String = "Accept",
    declineLabel: String = "Decline",
) {
    val colors = SetmonTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(colors.accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = nickname.take(1).uppercase(),
                style = SetmonTheme.typography.subtitle,
                color = colors.textPrimary,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(nickname, style = SetmonTheme.typography.subtitle, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Text(subtitle, style = SetmonTheme.typography.caption, color = colors.textSecondary)
            }
        }
        if (onAccept != null || onDecline != null) {
            if (onDecline != null) {
                TextButton(onClick = onDecline) {
                    Text(declineLabel, style = SetmonTheme.typography.label, color = colors.textSecondary)
                }
            }
            if (onAccept != null) {
                Box(
                    modifier = Modifier
                        .clip(SetmonTheme.shapes.pill)
                        .background(colors.primary)
                        .clickable(onClick = onAccept)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Text(acceptLabel, style = SetmonTheme.typography.label, color = colors.onPrimary)
                }
            }
        } else if (onRemove != null) {
            TextButton(onClick = onRemove) {
                Text(removeLabel, style = SetmonTheme.typography.label, color = colors.textSecondary)
            }
        }
    }
}

/* ---------- SettingRow (S-12) ---------- */

/** 마이페이지 스위치 한 줄 (높이 56). 피그마 `SettingRow` (Type=Switch). */
@Composable
fun SettingSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SetmonTheme.colors
    SettingRowFrame(label = label, modifier = modifier) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = colors.primary,
                checkedThumbColor = colors.surface,
                uncheckedTrackColor = colors.outline,
                uncheckedThumbColor = colors.surface,
                uncheckedBorderColor = colors.outline,
            ),
        )
    }
}

/** 마이페이지 시간 선택 한 줄. 피그마 `SettingRow` (Type=Time). 누르면 시간 선택 창은 화면 쪽에서 띄운다. */
@Composable
fun SettingTimeRow(
    label: String,
    timeText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SetmonTheme.colors
    SettingRowFrame(label = label, modifier = modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .background(colors.surface, SetmonTheme.shapes.thumbnail)
                .border(1.dp, colors.outline, SetmonTheme.shapes.thumbnail)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(timeText, style = SetmonTheme.typography.label, color = colors.primary)
        }
    }
}

@Composable
private fun SettingRowFrame(
    label: String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = SetmonTheme.typography.body, color = SetmonTheme.colors.textPrimary, modifier = Modifier.weight(1f))
        trailing()
    }
}

/* ---------- 미리보기 ---------- */

@Preview(name = "ClipTimelineItem", showBackground = true, backgroundColor = 0xFFF5F0E6)
@Composable
private fun ClipTimelineItemPreview() {
    SetmonTheme {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ClipTimelineItem("13:00", ClipSlotState.Filled, thumbnailUrl = "preview")
            ClipTimelineItem("14:00", ClipSlotState.Playing, thumbnailUrl = "preview")
            ClipTimelineItem("15:00", ClipSlotState.Empty)
        }
    }
}

@Preview(name = "FriendListItem", widthDp = 412, showBackground = true, backgroundColor = 0xFFF5F0E6)
@Composable
private fun FriendListItemPreview() {
    SetmonTheme {
        Column {
            FriendListItem("Taeyang", subtitle = "12 days recorded", onRemove = {})
            FriendListItem("Jisu", subtitle = "Wants to be friends", onAccept = {}, onDecline = {})
        }
    }
}

@Preview(name = "SettingRow", widthDp = 412, showBackground = true, backgroundColor = 0xFFF5F0E6)
@Composable
private fun SettingRowPreview() {
    SetmonTheme {
        Column {
            SettingSwitchRow("Recording reminders", checked = true, onCheckedChange = {})
            SettingSwitchRow("Vlog ready alerts", checked = false, onCheckedChange = {})
            SettingTimeRow("Reminders start", timeText = "09:00", onClick = {})
        }
    }
}
