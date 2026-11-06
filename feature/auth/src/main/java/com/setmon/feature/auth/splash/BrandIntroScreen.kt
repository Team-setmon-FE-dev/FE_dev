package com.setmon.feature.auth.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.setmon.core.designsystem.theme.PlayfairDisplay
import com.setmon.core.designsystem.theme.Pretendard
import com.setmon.core.designsystem.theme.SetmonTheme
import com.setmon.feature.auth.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import com.setmon.core.designsystem.R as DesignR

/**
 * S-00 브랜드 인트로 (피그마 `S-00 · Brand intro (planner)`).
 * 앱을 켜면 시스템 스플래시(로고) 다음에 약 1초 보이고, 저장된 로그인이 있으면 달력(S-03), 없으면 로그인(S-01)으로 간다.
 */
@Composable
internal fun SplashRoot(
    onNeedLogin: () -> Unit,
    onLoggedIn: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()
    LaunchedEffect(destination) {
        when (destination) {
            SplashDestination.Login -> onNeedLogin()
            SplashDestination.Calendar -> onLoggedIn()
            null -> Unit
        }
    }
    BrandIntroScreen()
}

/* ------------------------------------------------------------------------------------------
 * 화면은 피그마 프레임(412 × 917) 좌표 그대로 그리고, 기기 크기에 맞춰 통째로 확대·축소한다.
 * 그래서 아래 숫자들은 모두 피그마의 x, y, 너비, 높이와 같다.
 * ------------------------------------------------------------------------------------------ */
private const val DESIGN_W = 412f
private const val DESIGN_H = 917f
private val HOURS = listOf("09:00", "12:00", "15:00", "18:00", "21:00")
private val RING_Y = listOf(146, 210, 274, 592, 656, 720)

// 금속·영수증 종이는 디자인 시스템 색이 아니라 "물건의 색"이라 여기서만 쓴다
private val ChromeDark = Color(0xFF4A4A4A)
private val ChromeMid = Color(0xFF9C9C9C)
private val ChromeLight = Color(0xFFE4E4E4)
private val ReceiptPaper = Color(0xFFFCFCFB)

/** 피그마 단위(1 = 1px) → 실제 dp 로 바꿔 주는 자. */
private class Board(val scale: Float) {
    fun d(v: Number): Dp = (v.toFloat() * scale).dp
}

private fun Modifier.place(b: Board, x: Number, y: Number, w: Number, h: Number): Modifier =
    this.offset(b.d(x), b.d(y)).size(b.d(w), b.d(h))

/** 사용자 글자 크기 설정과 관계없이 그림처럼 고정된 크기로 그린다 (인트로는 읽는 화면이 아니라 보는 화면) */
@Composable
private fun Board.sp(v: Float): TextUnit = with(LocalDensity.current) { (v * scale).dp.toSp() }

@Composable
internal fun BrandIntroScreen(
    /** 나중에 "가장 최근 클립 썸네일"을 넣을 자리. 없으면 색 그라데이션. */
    photo: Painter? = null,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(SplashViewModel.MIN_SHOW_MS.toInt(), easing = FastOutSlowInEasing))
    }
    val colors = SetmonTheme.colors

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center,
    ) {
        val b = Board(min(maxWidth / DESIGN_W.dp, maxHeight / DESIGN_H.dp))
        Box(
            Modifier
                .size(b.d(DESIGN_W), b.d(DESIGN_H))
                .clipToBounds(),
        ) {
            LeatherCover(b)
            HourTabs(b)
            Page(b)
            BinderMetal(b)
            Receipt(b)
            Polaroid(b, photo)
            StarClip(b)
            Headline(b, progress.value)
        }
    }
}

@Composable
private fun LeatherCover(b: Board) {
    val colors = SetmonTheme.colors
    Box(
        Modifier
            .place(b, 28, 30, 400, 860)
            .clip(RoundedCornerShape(b.d(28)))
            .background(colors.primary)
            .background(
                Brush.horizontalGradient(
                    0f to Color.White.copy(alpha = 0.10f),
                    0.6f to Color.Transparent,
                    1f to Color.Black.copy(alpha = 0.18f),
                ),
            ),
    )
}

@Composable
private fun HourTabs(b: Board) {
    val colors = SetmonTheme.colors
    val shape = RoundedCornerShape(topEnd = b.d(10), bottomEnd = b.d(10))
    HOURS.forEachIndexed { i, hour ->
        Box(
            modifier = Modifier
                .place(b, 364, 86 + i * 150, 30, 144)
                .clip(shape)
                .background(if (i == 2) colors.accent else colors.surface)
                .border(1.dp, colors.outline, shape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = hour,
                style = TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = b.sp(10f),
                    letterSpacing = b.sp(2f),
                    color = colors.textPrimary,
                ),
                maxLines = 1,
                softWrap = false,
                // 위에서 아래로 읽히게 시계 방향 90도
                modifier = Modifier
                    .wrapContentSize(unbounded = true)
                    .rotate(90f),
            )
        }
    }
}

@Composable
private fun Page(b: Board) {
    val shape = RoundedCornerShape(topEnd = b.d(6), bottomEnd = b.d(6))
    Box(
        Modifier
            .place(b, 42, 44, 330, 830)
            .shadow(b.d(6), shape)
            .background(SetmonTheme.colors.surface, shape),
    )
}

/** 바인더 가운데 철: 크롬 기둥, 고리 6개, 리벳 */
@Composable
private fun BinderMetal(b: Board) {
    val colors = SetmonTheme.colors
    // 기둥
    Box(
        Modifier
            .place(b, 26, 44, 16, 830)
            .clip(RoundedCornerShape(50))
            .background(
                Brush.horizontalGradient(
                    listOf(ChromeDark, ChromeMid, Color.White, Color(0xFFC9C9C9), Color(0xFF7A7A7A), Color(0xFF3A3A3A)),
                ),
            ),
    )
    Box(
        Modifier
            .place(b, 32, 50, 1.6f, 818)
            .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(50)),
    )
    // 종이 구멍 + 고리
    RING_Y.forEach { y ->
        Box(
            Modifier
                .place(b, 60, y + 12, 9, 6)
                .background(colors.textPrimary.copy(alpha = 0.2f), CircleShape),
        )
        Canvas(Modifier.place(b, 0, y, 70, 30)) { drawChromeRing() }
    }
    // 리벳
    Canvas(Modifier.place(b, 21, 446, 26, 26)) { drawRivet() }
}

private fun DrawScope.drawChromeRing() {
    val sx = size.width / 70f
    val sy = size.height / 30f
    val topLeft = Offset(5f * sx, 5.5f * sy)
    val ovalSize = Size(60f * sx, 19f * sy)
    val stroke = 6f * sx
    // 그림자
    drawOval(
        color = Color.Black.copy(alpha = 0.22f),
        topLeft = topLeft + Offset(1f * sx, 3f * sy),
        size = ovalSize,
        style = Stroke(width = stroke),
    )
    // 몸통: 위는 밝고 아래로 갈수록 어두운 금속 띠
    drawOval(
        brush = Brush.verticalGradient(
            0f to Color(0xFFFDFDFD),
            0.3f to ChromeMid,
            0.55f to ChromeLight,
            0.8f to Color(0xFF6A6A6A),
            1f to Color(0xFFC8C8C8),
            startY = topLeft.y - stroke / 2,
            endY = topLeft.y + ovalSize.height + stroke / 2,
        ),
        topLeft = topLeft,
        size = ovalSize,
        style = Stroke(width = stroke),
    )
    // 윗면 하이라이트
    drawArc(
        color = Color.White.copy(alpha = 0.9f),
        startAngle = 200f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = topLeft + Offset(0f, -1.5f * sy),
        size = ovalSize,
        style = Stroke(width = 1.4f * sx, cap = StrokeCap.Round),
    )
    // 테두리
    drawOval(
        color = Color(0xFF3E3E3E).copy(alpha = 0.35f),
        topLeft = topLeft,
        size = ovalSize,
        style = Stroke(width = 0.8f * sx),
    )
}

private fun DrawScope.drawRivet() {
    val r = size.minDimension / 2f
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color.White, Color(0xFFBDBDBD), Color(0xFF4E4E4E)),
            center = Offset(size.width * 0.38f, size.height * 0.35f),
            radius = r * 1.4f,
        ),
        radius = r * 0.92f,
    )
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFFE9E9E9), Color(0xFF7C7C7C)),
            center = Offset(size.width * 0.6f, size.height * 0.6f),
            radius = r * 0.6f,
        ),
        radius = r * 0.46f,
    )
    drawCircle(color = Color(0xFF5C5C5C), radius = r * 0.46f, style = Stroke(width = r * 0.06f))
    drawCircle(color = Color.White, radius = r * 0.12f, center = Offset(size.width * 0.44f, size.height * 0.44f))
}

/** 폴라로이드 뒤에 끼운 감열지 영수증: 오늘의 시간별 기록처럼 보이게 */
@Composable
private fun Receipt(b: Board) {
    val colors = SetmonTheme.colors
    val now = remember { LocalDateTime.now() }
    val dateLine = remember(now) {
        now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH)).uppercase(Locale.ENGLISH)
    }
    val mono = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = b.sp(8.5f),
        lineHeight = b.sp(11.5f),
        color = colors.textPrimary,
    )
    val muted = mono.copy(color = colors.textSecondary)
    val bold = mono.copy(fontWeight = FontWeight.Bold)

    Column(
        Modifier
            .offset(b.d(170), b.d(226))
            .width(b.d(176))
            .graphicsLayer {
                rotationZ = 4f
                transformOrigin = TransformOrigin(0f, 0f)
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(b.d(4))
                .background(ReceiptPaper)
                // 구겨진 종이 느낌: 아주 옅은 사선 얼룩
                .background(
                    Brush.linearGradient(
                        0f to Color.Transparent,
                        0.3f to Color.Black.copy(alpha = 0.05f),
                        0.32f to Color.Transparent,
                        0.6f to Color.Black.copy(alpha = 0.04f),
                        0.63f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.06f),
                    ),
                )
                .padding(horizontal = b.d(14), vertical = b.d(14)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(b.d(5)),
        ) {
            Image(
                painter = painterResource(DesignR.drawable.setmon_monogram),
                contentDescription = null,
                modifier = Modifier.width(b.d(46)),
            )
            Text("setmon daily log", style = muted, textAlign = TextAlign.Center)
            Text("LOG #0006 FOR ★\n$dateLine", style = bold, modifier = Modifier.fillMaxWidth())
            ReceiptDivider(muted)
            ReceiptRow("HR", "MOMENT", "SEC", bold)
            listOf(
                Triple("09:00", "coffee", "1.6"),
                Triple("12:00", "lunch", "1.2"),
                Triple("15:00", "walk", "2.0"),
                Triple("18:00", "sunset", "1.8"),
                Triple("21:00", "home", "1.4"),
            ).forEach { (h, m, s) -> ReceiptRow(h, m, s, mono) }
            ReceiptDivider(muted)
            ReceiptRow("CLIP COUNT", "", "05", mono)
            ReceiptRow("TOTAL (vlog)", "", "8.0s", mono)
            ReceiptRow("MOOD", "", "CALM", mono)
            ReceiptDivider(muted)
            Text("THANK YOU FOR RECORDING!", style = muted, textAlign = TextAlign.Center, maxLines = 1)
            Canvas(
                Modifier
                    .padding(top = b.d(2))
                    .width(b.d(120))
                    .height(b.d(30)),
            ) { drawBarcode(colors.textPrimary) }
        }
        // 찢긴 아래 끝
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(b.d(6)),
        ) {
            val teeth = 22
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                for (i in teeth downTo 0) {
                    lineTo(size.width * i / teeth, if (i % 2 == 1) size.height else size.height * 0.15f)
                }
                close()
            }
            drawPath(path, ReceiptPaper)
        }
    }
}

@Composable
private fun ReceiptDivider(style: TextStyle) {
    Text(
        text = "- ".repeat(30),
        style = style,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ReceiptRow(left: String, middle: String, right: String, style: TextStyle) {
    Row(Modifier.fillMaxWidth()) {
        Text(left, style = style, maxLines = 1, modifier = Modifier.weight(if (middle.isEmpty()) 1f else 0.34f))
        if (middle.isNotEmpty()) Text(middle, style = style, maxLines = 1, modifier = Modifier.weight(0.46f))
        Text(right, style = style, maxLines = 1, textAlign = TextAlign.End, modifier = Modifier.weight(0.2f))
    }
}

private val BARCODE = intArrayOf(2, 1, 1, 3, 1, 2, 1, 1, 2, 3, 1, 1, 2, 1, 3, 1, 2, 1, 1, 2, 1, 3, 2, 1, 1, 2, 1, 1, 3, 1, 2, 1, 2)

private fun DrawScope.drawBarcode(color: Color) {
    val units = BARCODE.sum() + (BARCODE.size - 1) * 1.5f
    val unit = size.width / units
    var x = 0f
    BARCODE.forEach { w ->
        drawRect(color, topLeft = Offset(x, 0f), size = Size(w * unit, size.height))
        x += (w + 1.5f) * unit
    }
}

@Composable
private fun Polaroid(b: Board, photo: Painter?) {
    val colors = SetmonTheme.colors
    val now = remember { LocalDateTime.now() }
    val caption = remember(now) {
        now.format(DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)).lowercase(Locale.ENGLISH) +
            " · %02d:00".format(now.hour)
    }
    Box(
        Modifier
            .place(b, 118, 96, 212, 246)
            .graphicsLayer { rotationZ = -3f }
            .shadow(b.d(10), RoundedCornerShape(b.d(3)))
            .background(colors.surface),
    ) {
        Box(Modifier.place(b, 10, 10, 192, 196)) {
            if (photo != null) {
                Image(photo, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(colors.accent, colors.secondary, colors.primary))),
                )
            }
            // 반투명 트레이싱지
            Box(
                Modifier
                    .fillMaxSize()
                    .background(colors.surface.copy(alpha = 0.5f)),
            )
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.auth_brand),
                    style = TextStyle(
                        fontFamily = PlayfairDisplay,
                        fontSize = b.sp(40f),
                        lineHeight = b.sp(42f),
                        letterSpacing = b.sp(-0.6f),
                        color = colors.textPrimary,
                    ),
                )
                Text(
                    text = stringResource(R.string.splash_wordmark_tagline),
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = b.sp(6.2f),
                        letterSpacing = b.sp(2.2f),
                        color = colors.textSecondary,
                    ),
                    maxLines = 1,
                )
            }
        }
        Text(
            text = caption,
            style = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = b.sp(10f),
                letterSpacing = b.sp(1.5f),
                color = colors.textSecondary,
            ),
            modifier = Modifier.offset(b.d(12), b.d(220)),
        )
    }
}

/** 별 모양 철사 클립 (폴라로이드와 영수증을 함께 물고 있다) */
@Composable
private fun StarClip(b: Board) {
    Canvas(
        Modifier
            .place(b, 190, 62, 64, 64)
            .graphicsLayer { rotationZ = -10f },
    ) {
        val k = size.width / 50f
        val star = Path().apply {
            for (i in 0 until 10) {
                val angle = -PI / 2 + i * PI / 5
                val radius = if (i % 2 == 1) 10f else 22f
                val x = (25f + radius * cos(angle).toFloat()) * k
                val y = (25f + radius * sin(angle).toFloat()) * k
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        val loop = Path().apply {
            moveTo(25f * k, 3.5f * k)
            cubicTo(19f * k, 12f * k, 18.5f * k, 27f * k, 22.5f * k, 32.5f * k)
            cubicTo(25.5f * k, 36.5f * k, 29.5f * k, 33f * k, 29f * k, 25.5f * k)
            lineTo(27.2f * k, 13f * k)
        }
        val wire = Stroke(width = 2.5f * k, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val silver = Brush.linearGradient(
            0f to Color(0xFFF4F4F4),
            0.45f to Color(0xFF8E8E8E),
            0.6f to Color(0xFFD8D8D8),
            1f to Color(0xFF6A6A6A),
            start = Offset.Zero,
            end = Offset(size.width, size.height),
        )
        // 그림자 먼저
        translate(left = 1f * k, top = 2f * k) {
            drawPath(star, Color.Black.copy(alpha = 0.2f), style = wire)
            drawPath(loop, Color.Black.copy(alpha = 0.2f), style = wire)
        }
        drawPath(star, silver, style = wire)
        drawPath(loop, silver, style = wire)
    }
}

@Composable
private fun Headline(b: Board, progress: Float) {
    val colors = SetmonTheme.colors
    val serif = TextStyle(
        fontFamily = PlayfairDisplay,
        fontSize = b.sp(50f),
        lineHeight = b.sp(56f),
        letterSpacing = b.sp(-1f),
        color = colors.textPrimary,
        textAlign = TextAlign.End,
    )
    Column(
        modifier = Modifier
            .offset(b.d(42), b.d(636))
            .width(b.d(310)),
        horizontalAlignment = Alignment.End,
    ) {
        // Playfair 이탤릭 글꼴 파일이 없어서 기울임은 시스템이 만들어 준다(합성 이탤릭)
        Text(stringResource(R.string.splash_headline_1), style = serif.copy(fontStyle = FontStyle.Italic), maxLines = 1)
        Text(stringResource(R.string.splash_headline_2), style = serif, maxLines = 1)
        Spacer(Modifier.height(b.d(16)))
        Text(
            text = stringResource(R.string.splash_subtitle),
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = b.sp(15f),
                lineHeight = b.sp(22f),
                color = colors.textPrimary,
            ),
        )
        Spacer(Modifier.height(b.d(26)))
        // 로딩 바
        Box(
            Modifier
                .width(b.d(120))
                .height(b.d(2))
                .background(colors.outline),
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(colors.primary),
            )
        }
    }
}

@Preview(widthDp = 412, heightDp = 917, showBackground = true)
@Composable
private fun BrandIntroPreview() {
    SetmonTheme { BrandIntroScreen() }
}

@Preview(name = "small phone", widthDp = 360, heightDp = 740, showBackground = true)
@Composable
private fun BrandIntroSmallPreview() {
    SetmonTheme { BrandIntroScreen() }
}
