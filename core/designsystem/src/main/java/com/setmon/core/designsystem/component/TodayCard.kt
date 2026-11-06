package com.setmon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.setmon.core.designsystem.theme.SetmonTheme

/**
 * S-03 달력 카드 아래 "오늘" 영역. 피그마 `TodayCard` (Mode=Mine / ReadOnly).
 * 친구 달력(readOnly = true)에서는 촬영 버튼을 숨긴다.
 */
@Composable
fun TodayCard(
    title: String,
    clipCount: Int,
    readOnly: Boolean,
    onRecordClick: () -> Unit,
    modifier: Modifier = Modifier,
    recordLabel: String = "Record a moment",
) {
    val colors = SetmonTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = SetmonTheme.typography.title,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f, fill = false),
            )
            Box(
                modifier = Modifier
                    .background(colors.primary, SetmonTheme.shapes.pill)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = if (clipCount == 1) "1 clip" else "$clipCount clips",
                    style = SetmonTheme.typography.label,
                    color = colors.onPrimary,
                )
            }
        }
        if (!readOnly) {
            SetmonButton(
                text = recordLabel,
                onClick = onRecordClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFBF8F2)
@Composable
private fun TodayCardPreview() {
    SetmonTheme {
        Column {
            TodayCard(title = "Today, November 4", clipCount = 3, readOnly = false, onRecordClick = {})
            TodayCard(title = "Today, November 4", clipCount = 1, readOnly = true, onRecordClick = {})
        }
    }
}
