package com.setmon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.setmon.core.designsystem.theme.SetmonTheme

/**
 * S-03 상단 친구 책갈피 탭 하나. 피그마 컴포넌트 `BookmarkTab` (State=Selected/Default).
 * - 선택됨: primary 배경 + onPrimary 글자
 * - 기본: surface 배경 + textPrimary 글자 (secondary 위 흰 글씨 금지 규칙)
 * 친구 추가용 "+" 탭은 label = "+" 인 기본 탭으로 쓴다.
 */
@Composable
fun BookmarkTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SetmonTheme.colors
    val shape = SetmonTheme.shapes.bookmarkTab
    Box(
        modifier = modifier
            .height(28.dp)
            .widthIn(min = 56.dp)
            .shadow(elevation = 2.dp, shape = shape, ambientColor = colors.shadow, spotColor = colors.shadow)
            .clip(shape)
            .background(if (selected) colors.primary else colors.surface)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = SetmonTheme.typography.label,
            color = if (selected) colors.onPrimary else colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E6)
@Composable
private fun BookmarkTabPreview() {
    SetmonTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BookmarkTab(label = "Me", selected = true, onClick = {})
            BookmarkTab(label = "Taeyang", selected = false, onClick = {})
            BookmarkTab(label = "+", selected = false, onClick = {})
        }
    }
}
