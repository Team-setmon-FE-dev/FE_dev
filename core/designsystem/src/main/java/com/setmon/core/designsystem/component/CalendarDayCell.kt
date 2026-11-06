package com.setmon.core.designsystem.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.setmon.core.designsystem.theme.SetmonTheme

/** 피그마 `CalendarDayCell` 의 State 와 같다. */
enum class DayCellState {
    /** 오늘. 테두리 + 클립 수 배지 */
    Today,
    /** 지난 날, 브이로그(또는 첫 클립) 썸네일 있음 */
    Thumbnail,
    /** 지난 날, 기록 없음 */
    Empty,
    /** 아직 오지 않은 날. 날짜를 흐리게 */
    Future,
    /** 다른 달 자리. 아무것도 안 그린다 */
    Blank,
}

/**
 * S-03 달력 칸 하나 (48 × 56dp). 7칸을 Row 로 놓으면 한 주가 된다.
 * 썸네일에는 톤 통일 필터(채도 -35, 대비 -10, 노출 +5)를 건다.
 */
@Composable
fun CalendarDayCell(
    day: Int?,
    state: DayCellState,
    modifier: Modifier = Modifier,
    thumbnailUrl: String? = null,
    clipCount: Int = 0,
    onClick: (() -> Unit)? = null,
) {
    val colors = SetmonTheme.colors
    val shapes = SetmonTheme.shapes
    val hasThumb = thumbnailUrl != null && (state == DayCellState.Thumbnail || state == DayCellState.Today)

    Box(
        modifier = modifier
            .size(width = 48.dp, height = 56.dp)
            .clip(shapes.thumbnail)
            .then(
                if (state == DayCellState.Today) Modifier.border(1.5.dp, colors.primary, shapes.thumbnail)
                else Modifier,
            )
            .then(
                if (onClick != null && state != DayCellState.Blank && state != DayCellState.Future) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            ),
    ) {
        if (state == DayCellState.Blank || day == null) return@Box

        if (hasThumb) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                colorFilter = SetmonThumbnailFilter,
                // 불러오는 중·실패·Preview 에서는 outline 색 네모로 자리를 보여준다
                placeholder = ColorPainter(colors.outline),
                error = ColorPainter(colors.outline),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
                    .clip(shapes.thumbnailInner),
            )
        }

        val dateColor = when {
            hasThumb -> colors.onPrimary
            state == DayCellState.Future -> colors.textSecondary
            else -> colors.textPrimary
        }
        Text(
            text = day.toString(),
            style = SetmonTheme.typography.label.copy(
                shadow = if (hasThumb) Shadow(colors.shadow.copy(alpha = 0.5f), Offset(0f, 1f), 3f) else null,
            ),
            color = dateColor,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp),
        )

        if (state == DayCellState.Today && clipCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 3.dp, bottom = 3.dp)
                    .size(15.dp)
                    .background(colors.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = clipCount.toString(),
                    style = SetmonTheme.typography.caption,
                    color = colors.onPrimary,
                )
            }
        }
    }
}

/** 썸네일 톤 통일: 채도 -35%, 대비 -10%, 노출 +5% (피그마와 같은 값) */
val SetmonThumbnailFilter: ColorFilter = run {
    val saturation = ColorMatrix().apply { setToSaturation(0.65f) }
    val contrast = 0.9f
    val offset = 255f * (1f - contrast) / 2f + 255f * 0.05f
    val contrastAndExposure = ColorMatrix(
        floatArrayOf(
            contrast, 0f, 0f, 0f, offset,
            0f, contrast, 0f, 0f, offset,
            0f, 0f, contrast, 0f, offset,
            0f, 0f, 0f, 1f, 0f,
        ),
    )
    saturation.timesAssign(contrastAndExposure)
    ColorFilter.colorMatrix(saturation)
}

@Preview(showBackground = true, backgroundColor = 0xFFFBF8F2)
@Composable
private fun CalendarDayCellPreview() {
    SetmonTheme {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            CalendarDayCell(day = 4, state = DayCellState.Today, thumbnailUrl = "preview", clipCount = 3)
            CalendarDayCell(day = 3, state = DayCellState.Thumbnail, thumbnailUrl = "preview")
            CalendarDayCell(day = 2, state = DayCellState.Empty)
            CalendarDayCell(day = 5, state = DayCellState.Future)
            CalendarDayCell(day = null, state = DayCellState.Blank)
        }
    }
}
