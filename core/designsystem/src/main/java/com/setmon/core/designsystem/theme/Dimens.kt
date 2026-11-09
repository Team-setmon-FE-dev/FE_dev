package com.setmon.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 간격은 4의 배수만 쓴다. 화면 좌우 여백은 [screenHorizontal]. */
@Immutable
data class SetmonSpacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 20.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val screenHorizontal: Dp = 16.dp,
)

/** 모서리: 버튼 12 / 카드 16 / 썸네일 6 (썸네일 안쪽 사진은 3) */
@Immutable
data class SetmonShapes(
    val button: RoundedCornerShape = RoundedCornerShape(12.dp),
    val card: RoundedCornerShape = RoundedCornerShape(16.dp),
    val thumbnail: RoundedCornerShape = RoundedCornerShape(6.dp),
    val thumbnailInner: RoundedCornerShape = RoundedCornerShape(3.dp),
    /** 책갈피 탭: 아래쪽 두 모서리만 둥글다 */
    val bookmarkTab: RoundedCornerShape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
    val pill: RoundedCornerShape = RoundedCornerShape(percent = 50),
)
