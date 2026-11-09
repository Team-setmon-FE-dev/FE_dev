package com.setmon.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.setmon.core.designsystem.R

/*
 * 폰트 파일은 core/designsystem/src/main/res/font 에 있다.
 * - Playfair Display, Caveat: Google Fonts (OFL) 가변 폰트에서 Regular(400)만 뽑은 파일
 * - Pretendard: orioncactus/pretendard v1.3.9 (OFL)
 * 한글은 Playfair·Caveat 에 글리프가 없어서 시스템 폰트로 대체된다.
 * 한글 세리프(Gowun Batang)·손글씨(Nanum Pen Script)가 필요해지면 여기에 Font 를 추가한다.
 */

val PlayfairDisplay = FontFamily(
    Font(R.font.playfair_display_regular, FontWeight.Normal),
)

val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
)

val Caveat = FontFamily(
    Font(R.font.caveat_regular, FontWeight.Normal),
)

/**
 * setmon 글자 스타일. 피그마 Text Styles 와 이름·크기·굵기가 같다.
 * 줄 높이(lineHeight)는 피그마에 따로 정하지 않아 크기의 약 1.2~1.45배로 잡았다.
 * 화면에서는 `SetmonTheme.typography.title` 처럼 꺼내 쓴다.
 */
@Immutable
data class SetmonTypography(
    /** 큰 월 이름 (November) */
    val display: TextStyle = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 44.sp,
        lineHeight = 52.sp,
    ),
    /** 카드 제목 (Today, November 4) */
    val title: TextStyle = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    val subtitle: TextStyle = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
    ),
    val body: TextStyle = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    /** 버튼 글자, 요일, 책갈피 이름 */
    val label: TextStyle = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    /** 배지, 작은 설명 */
    val caption: TextStyle = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
    ),
    /** 테이프·스티커 위 손글씨 */
    val handwriting: TextStyle = TextStyle(
        fontFamily = Caveat,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 22.sp,
    ),
)
