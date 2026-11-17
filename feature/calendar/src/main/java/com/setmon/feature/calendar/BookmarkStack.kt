package com.setmon.feature.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import com.setmon.core.designsystem.theme.SetmonTheme

/** 책갈피 한 줄 */
internal data class BookmarkItem(
    val key: Any,
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit,
)

private val ROW_STEP = 26.dp // 줄과 줄 사이
private val BAND_HEIGHT = 32.dp // 가로 전체 밴드. ROW_STEP 보다 커야 아래 줄 밴드 밑으로 들어가 틈이 안 생긴다
private val TAB_HEIGHT = 44.dp // 밴드 윗선부터 튀어나온 탭 아랫선까지 (밴드 아래로 12dp)
private val TAB_CORNER = 8.dp
private val TAB_TEXT_PADDING = 20.dp
private val TAB_MIN_WIDTH = 96.dp
private val TAB_MAX_WIDTH = 168.dp
private val TAB_EDGE = 12.dp
/** 그림자: 아래로 조금씩 더 내려가며 옅게 여러 번 칠해 번지는 모양을 만든다 (기기 버전과 상관없이 같은 모양) */
private val SHADOW_OFFSET = 4.dp
private const val SHADOW_LAYERS = 4
private const val SHADOW_LAYER_ALPHA = 0.07f

/** 친구가 0~2명이어도 달력이 위로 몰리지 않게 이만큼의 줄 높이는 비워 둔다 */
private const val MIN_ROWS = 4

/** 탭이 줄마다 엇갈리는 가로 위치 (화면 폭 비율). 피그마 S-03 · Friend tabs 의 위치 */
private val TAB_X_FRACTIONS = listOf(0.06f, 0.52f, 0.23f, 0.62f, 0.36f)

/**
 * S-03 맨 위 책갈피. 피그마 `S-03 · Main calendar` > Friend tabs.
 * 가로 전체 밴드가 위에서부터 한 줄씩 쌓이고, 줄마다 밴드 아래로 탭이 튀어나온다.
 * 위 줄이 아래 줄을 덮는다. 첫 줄 밴드는 상태 표시줄 뒤까지 칠한다.
 * 친구가 늘면 줄이 아래로 늘어난다.
 */
@Composable
internal fun BookmarkStack(
    items: List<BookmarkItem>,
    modifier: Modifier = Modifier,
) {
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    StatusBarIconsOn(darkBackground = items.firstOrNull()?.selected == true)
    val rows = maxOf(items.size, MIN_ROWS)
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(statusTop + ROW_STEP * (rows - 1) + TAB_HEIGHT + SHADOW_OFFSET * 2),
    ) {
        val width = maxWidth
        items.forEachIndexed { index, item ->
            BookmarkRow(
                item = item,
                fraction = TAB_X_FRACTIONS[index % TAB_X_FRACTIONS.size],
                screenWidth = width,
                // 첫 줄은 상태 표시줄 높이만큼 위로 늘려 화면 맨 위부터 칠한다
                top = if (index == 0) 0.dp else statusTop + ROW_STEP * index,
                extraTop = if (index == 0) statusTop else 0.dp,
                modifier = Modifier.zIndex((items.size - index).toFloat()),
            )
        }
    }
}

/** 첫 줄이 초록이면 상태 표시줄 아이콘을 밝게. 화면을 떠나면 원래대로 돌려 놓는다 */
@Composable
private fun StatusBarIconsOn(darkBackground: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    DisposableEffect(view, darkBackground) {
        val window = (view.context as? Activity)?.window ?: return@DisposableEffect onDispose {}
        val controller = WindowCompat.getInsetsController(window, view)
        val before = controller.isAppearanceLightStatusBars
        controller.isAppearanceLightStatusBars = !darkBackground
        onDispose { controller.isAppearanceLightStatusBars = before }
    }
}

@Composable
private fun BookmarkRow(
    item: BookmarkItem,
    fraction: Float,
    screenWidth: Dp,
    top: Dp,
    extraTop: Dp,
    modifier: Modifier = Modifier,
) {
    val colors = SetmonTheme.colors
    val textStyle = SetmonTheme.typography.label
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()

    val textWidth = remember(item.label, textStyle, density) {
        with(density) { measurer.measure(item.label, textStyle, maxLines = 1).size.width.toDp() }
    }
    val tabWidth = (textWidth + TAB_TEXT_PADDING * 2).coerceIn(TAB_MIN_WIDTH, TAB_MAX_WIDTH)
    val tabX = (screenWidth * fraction)
        .coerceAtMost(screenWidth - tabWidth - TAB_EDGE)
        .coerceAtLeast(TAB_EDGE)

    val fill = if (item.selected) colors.primary else colors.surface
    val shadow = colors.shadow.copy(alpha = SHADOW_LAYER_ALPHA)

    Box(
        modifier
            .offset(y = top)
            .fillMaxWidth()
            .height(extraTop + TAB_HEIGHT + SHADOW_OFFSET * 2)
            .drawBehind {
                // 밴드(가로 전체) + 탭(아래 모서리만 둥글게)을 한 모양으로 칠한다
                val bandBottom = (extraTop + BAND_HEIGHT).toPx()
                val tabLeft = tabX.toPx()
                val corner = CornerRadius(TAB_CORNER.toPx())
                val path = Path().apply {
                    addRect(Rect(0f, 0f, size.width, bandBottom))
                    addRoundRect(
                        RoundRect(
                            left = tabLeft,
                            top = 0f,
                            right = tabLeft + tabWidth.toPx(),
                            bottom = (extraTop + TAB_HEIGHT).toPx(),
                            topLeftCornerRadius = CornerRadius.Zero,
                            topRightCornerRadius = CornerRadius.Zero,
                            bottomRightCornerRadius = corner,
                            bottomLeftCornerRadius = corner,
                        ),
                    )
                }
                // 아래 줄 위로 번지는 그림자
                val step = SHADOW_OFFSET.toPx() / SHADOW_LAYERS
                for (layer in 1..SHADOW_LAYERS) {
                    translate(top = step * layer) { drawPath(path, shadow) }
                }
                drawPath(path, fill)
            },
    ) {
        Box(
            modifier = Modifier
                .offset(x = tabX, y = extraTop)
                .size(width = tabWidth, height = TAB_HEIGHT)
                .clip(RoundedCornerShape(bottomStart = TAB_CORNER, bottomEnd = TAB_CORNER))
                .semantics { selected = item.selected }
                .clickable(role = Role.Tab, onClick = item.onClick)
                .padding(horizontal = 8.dp)
                .padding(bottom = 7.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Text(
                text = item.label,
                style = textStyle,
                color = if (item.selected) colors.onPrimary else colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFEDEAE4)
@Composable
private fun BookmarkStackPreview() {
    SetmonTheme {
        BookmarkStack(
            items = listOf(
                BookmarkItem("me", "마이페이지", selected = true, onClick = {}),
                BookmarkItem(3L, "Taeyang", selected = false, onClick = {}),
                BookmarkItem(2L, "Minjun", selected = false, onClick = {}),
                BookmarkItem(4L, "Jisu", selected = false, onClick = {}),
                BookmarkItem("add", "+", selected = false, onClick = {}),
            ),
        )
    }
}
