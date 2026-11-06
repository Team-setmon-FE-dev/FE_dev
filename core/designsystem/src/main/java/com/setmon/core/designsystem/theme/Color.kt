package com.setmon.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * setmon 색 토큰. 피그마 Variables `setmon/color` 와 이름·값이 같다.
 * 화면에서는 `SetmonTheme.colors.primary` 처럼 꺼내 쓴다.
 */
@Immutable
data class SetmonColors(
    /** 촬영 버튼, 오늘, 선택 상태 */
    val primary: Color = Color(0xFF1E3B2F),
    /** primary 위 글자·아이콘 (surface 와 같은 종이색) */
    val onPrimary: Color = Color(0xFFFBF8F2),
    /** 보조 아이콘·강조선. 이 색 위에 흰 작은 글씨 금지 */
    val secondary: Color = Color(0xFF7F8C6A),
    /** 테이프, 스티커, 배지 */
    val accent: Color = Color(0xFFC8B48A),
    /** 화면 배경 */
    val background: Color = Color(0xFFF5F0E6),
    /** 카드, 달력 종이, 입력창 */
    val surface: Color = Color(0xFFFBF8F2),
    /** 칸 선, 구분선 */
    val outline: Color = Color(0xFFDDD5C4),
    /** 본문 */
    val textPrimary: Color = Color(0xFF1A1A1A),
    /** 요일, 설명, 미래 날짜. 사진 위 글자에는 쓰지 않는다 */
    val textSecondary: Color = Color(0xFF6B675F),
    /** 오류 */
    val error: Color = Color(0xFFA8432F),
) {
    /** 사진 위 글자 받침용 테이프 (accent 85%) */
    val tape: Color get() = accent.copy(alpha = 0.85f)

    /** 그림자 색. #000 대신 textPrimary 를 쓴다 */
    val shadow: Color get() = textPrimary
}
