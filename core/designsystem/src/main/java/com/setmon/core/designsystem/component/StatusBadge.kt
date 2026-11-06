package com.setmon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.setmon.core.designsystem.theme.SetmonTheme

/** 하루 브이로그 상태. 서버 daily_vlogs.status 와 대응 (PENDING·PROCESSING → Generating) */
enum class VlogStatus { Generating, Done, Failed }

/** S-05 브이로그 상태 배지. 피그마 `StatusBadge` (Status=Generating/Done/Failed). */
@Composable
fun StatusBadge(
    status: VlogStatus,
    modifier: Modifier = Modifier,
    label: String = when (status) {
        VlogStatus.Generating -> "Generating"
        VlogStatus.Done -> "Ready"
        VlogStatus.Failed -> "Failed"
    },
) {
    val colors = SetmonTheme.colors
    val (bg, fg) = when (status) {
        VlogStatus.Generating -> colors.accent to colors.textPrimary
        VlogStatus.Done -> colors.primary to colors.onPrimary
        VlogStatus.Failed -> colors.error to colors.onPrimary
    }
    Box(
        modifier = modifier
            .background(bg, SetmonTheme.shapes.pill)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(label, style = SetmonTheme.typography.caption, color = fg)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E6)
@Composable
private fun StatusBadgePreview() {
    SetmonTheme {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VlogStatus.entries.forEach { StatusBadge(it) }
        }
    }
}
