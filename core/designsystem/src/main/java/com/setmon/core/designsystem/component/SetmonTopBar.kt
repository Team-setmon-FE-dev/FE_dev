package com.setmon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.setmon.core.designsystem.theme.SetmonTheme

/**
 * 상단바. 피그마 `TopBar` (Type=Back / BackWithAction / ReadOnly).
 * - [actionLabel] 이 있으면 오른쪽에 글자 버튼 (BackWithAction)
 * - [readOnly] 면 오른쪽에 Read-only 표시 (친구 달력·하루 상세)
 */
@Composable
fun SetmonTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
    readOnly: Boolean = false,
) {
    val colors = SetmonTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(colors.background)
            .padding(horizontal = 4.dp),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
            BackArrow(color = colors.textPrimary)
        }
        Text(
            text = title,
            style = SetmonTheme.typography.subtitle,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 96.dp),
        )
        when {
            readOnly -> Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .background(colors.tape, SetmonTheme.shapes.thumbnail)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text("Read-only", style = SetmonTheme.typography.caption, color = colors.textPrimary)
            }
            actionLabel != null -> TextButton(
                onClick = onAction,
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                Text(actionLabel, style = SetmonTheme.typography.label, color = colors.primary)
            }
        }
    }
}

/** 아이콘 라이브러리 없이 그리는 뒤로 화살표 (24dp) */
@Composable
private fun BackArrow(color: androidx.compose.ui.graphics.Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width
        val path = Path().apply {
            moveTo(w * 0.8f, w * 0.5f)
            lineTo(w * 0.2f, w * 0.5f)
            moveTo(w * 0.45f, w * 0.25f)
            lineTo(w * 0.2f, w * 0.5f)
            lineTo(w * 0.45f, w * 0.75f)
        }
        drawPath(path, color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Preview(showBackground = true)
@Composable
private fun SetmonTopBarPreview() {
    SetmonTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SetmonTopBar(title = "Friends", onBack = {})
            SetmonTopBar(title = "November 4", onBack = {}, actionLabel = "Save")
            SetmonTopBar(title = "Taeyang's day", onBack = {}, readOnly = true)
        }
    }
}
