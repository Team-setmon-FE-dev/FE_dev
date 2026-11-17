package com.setmon.feature.calendar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.setmon.core.designsystem.component.CalendarDayCell
import com.setmon.core.designsystem.component.SetmonButton
import com.setmon.core.designsystem.component.SetmonButtonType
import com.setmon.core.designsystem.component.TodayCard
import com.setmon.core.designsystem.theme.SetmonTheme
import com.setmon.core.model.Friend
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle as DateTextStyle
import java.util.Locale

private val SWIPE_THRESHOLD = 64.dp
private const val BACKGROUND_OVERLAY_ALPHA = 0.53f // 피그마의 Overlay 28% + BackgroundFilter 35%

/** S-03 달력. 피그마 `S-03 · Main calendar` */
@Composable
internal fun CalendarRoot(
    userId: Long,
    readOnly: Boolean,
    friendNickname: String?,
    onDayClick: (date: String) -> Unit,
    onCaptureClick: () -> Unit,
    onFriendCalendarClick: (friendUserId: Long, nickname: String) -> Unit,
    onMyCalendarClick: () -> Unit,
    onFriendClick: () -> Unit,
    onMyPageClick: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

    CalendarScreen(
        uiState = uiState,
        userId = userId,
        readOnly = readOnly,
        friendNickname = friendNickname,
        onPreviousMonth = viewModel::showPreviousMonth,
        onNextMonth = viewModel::showNextMonth,
        onRetry = viewModel::retry,
        onDayClick = onDayClick,
        onCaptureClick = onCaptureClick,
        onFriendCalendarClick = onFriendCalendarClick,
        onMyCalendarClick = onMyCalendarClick,
        onFriendClick = onFriendClick,
        onMyPageClick = onMyPageClick,
    )
}

@Composable
private fun CalendarScreen(
    uiState: CalendarUiState,
    userId: Long,
    readOnly: Boolean,
    friendNickname: String?,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onRetry: () -> Unit,
    onDayClick: (date: String) -> Unit,
    onCaptureClick: () -> Unit,
    onFriendCalendarClick: (friendUserId: Long, nickname: String) -> Unit,
    onMyCalendarClick: () -> Unit,
    onFriendClick: () -> Unit,
    onMyPageClick: () -> Unit,
) {
    val colors = SetmonTheme.colors
    val shape = SetmonTheme.shapes.thumbnail
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.calendar_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(colors.background.copy(alpha = BACKGROUND_OVERLAY_ALPHA)),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                // 위쪽은 책갈피가 상태 표시줄 뒤까지 칠하므로 아래쪽만 띄운다
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            BookmarkStack(
                items = bookmarkItems(
                    userId = userId,
                    readOnly = readOnly,
                    friendNickname = friendNickname,
                    friends = uiState.friends,
                    onMyCalendarClick = onMyCalendarClick,
                    onMyPageClick = onMyPageClick,
                    onFriendCalendarClick = onFriendCalendarClick,
                    onFriendClick = onFriendClick,
                ),
            )

            MonthHeading(month = uiState.month, onPrevious = onPreviousMonth, onNext = onNextMonth)

            Box(Modifier.fillMaxWidth().padding(top = 36.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SetmonTheme.spacing.screenHorizontal)
                        .shadow(elevation = 4.dp, shape = shape)
                        .background(colors.surface, shape)
                        .padding(horizontal = 4.dp, vertical = 12.dp),
                ) {
                    WeekdayHeader()
                    Rule()
                    if (uiState.isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = colors.primary,
                            trackColor = colors.outline,
                        )
                    }
                    CalendarWeeks(
                        weeks = uiState.weeks,
                        onDayClick = onDayClick,
                        onSwipeToPrevious = onPreviousMonth,
                        onSwipeToNext = onNextMonth,
                    )
                    uiState.error?.let { error ->
                        Text(
                            text = stringResource(error),
                            style = SetmonTheme.typography.label,
                            color = colors.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        )
                        SetmonButton(
                            text = stringResource(R.string.calendar_retry),
                            onClick = onRetry,
                            type = SetmonButtonType.Text,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        )
                    }
                    Rule()
                    TodayCard(
                        title = stringResource(
                            R.string.calendar_today_title,
                            uiState.today.monthValue,
                            uiState.today.dayOfMonth,
                        ),
                        clipCount = uiState.todayClipCount,
                        readOnly = readOnly,
                        onRecordClick = onCaptureClick,
                        recordLabel = stringResource(R.string.calendar_record),
                        clipCountLabel = stringResource(R.string.calendar_clip_count, uiState.todayClipCount),
                    )
                }
                // 금속 클립이 달력 종이 윗부분을 집고 있는 모양
                Image(
                    painter = painterResource(R.drawable.calendar_clip),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-30).dp)
                        .size(width = 72.dp, height = 36.dp),
                )
            }

            Text(
                text = "setmon",
                style = SetmonTheme.typography.title,
                color = colors.primary,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 12.dp, end = 20.dp, bottom = 16.dp),
            )
        }
    }
}

/**
 * 책갈피 줄: 마이페이지 · 친구들(닉네임순) · +
 * - 내 달력: 마이페이지 줄이 선택됨. 누르면 S-12, 친구 줄은 그 친구 달력
 * - 친구 달력: 그 친구 줄이 선택됨. 마이페이지 줄은 내 달력으로, 다른 친구 줄은 그 친구 달력으로 바꾼다
 */
@Composable
private fun bookmarkItems(
    userId: Long,
    readOnly: Boolean,
    friendNickname: String?,
    friends: List<Friend>,
    onMyCalendarClick: () -> Unit,
    onMyPageClick: () -> Unit,
    onFriendCalendarClick: (friendUserId: Long, nickname: String) -> Unit,
    onFriendClick: () -> Unit,
): List<BookmarkItem> {
    val myLabel = stringResource(R.string.calendar_my_page)
    // 친구 목록을 받기 전에도 지금 보는 친구 줄은 보이게 한다
    val shownFriends = if (readOnly && friendNickname != null && friends.none { it.userId == userId }) {
        (friends + Friend(userId, friendNickname)).sortedBy { it.nickname }
    } else {
        friends
    }
    return buildList {
        add(
            BookmarkItem(
                key = "me",
                label = myLabel,
                selected = !readOnly,
                onClick = if (readOnly) onMyCalendarClick else onMyPageClick,
            ),
        )
        shownFriends.forEach { friend ->
            val selected = readOnly && friend.userId == userId
            add(
                BookmarkItem(
                    key = friend.userId,
                    label = friend.nickname,
                    selected = selected,
                    onClick = { if (!selected) onFriendCalendarClick(friend.userId, friend.nickname) },
                ),
            )
        }
        add(BookmarkItem(key = "add", label = "+", selected = false, onClick = onFriendClick))
    }
}

@Composable
private fun MonthHeading(month: YearMonth, onPrevious: () -> Unit, onNext: () -> Unit) {
    val colors = SetmonTheme.colors
    val typography = SetmonTheme.typography
    val previousLabel = stringResource(R.string.calendar_previous_month)
    val nextLabel = stringResource(R.string.calendar_next_month)
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onPrevious,
                modifier = Modifier.semantics { contentDescription = previousLabel },
            ) {
                Text("‹", style = typography.title, color = colors.textPrimary)
            }
            Text(
                text = month.month.getDisplayName(DateTextStyle.FULL, Locale.ENGLISH),
                style = typography.display,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = onNext,
                modifier = Modifier.semantics { contentDescription = nextLabel },
            ) {
                Text("›", style = typography.title, color = colors.textPrimary)
            }
        }
        Box(
            modifier = Modifier
                .rotate(-2f)
                .background(colors.accent)
                .padding(horizontal = 12.dp),
        ) {
            Text(text = month.year.toString(), style = typography.title, color = colors.textPrimary)
        }
    }
}

@Composable
private fun WeekdayHeader() {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        stringArrayResource(R.array.calendar_weekdays).forEach { weekday ->
            Text(
                text = weekday,
                style = SetmonTheme.typography.label,
                color = SetmonTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CalendarWeeks(
    weeks: List<List<CalendarCell>>,
    onDayClick: (date: String) -> Unit,
    onSwipeToPrevious: () -> Unit,
    onSwipeToNext: () -> Unit,
) {
    val swipeThresholdPx = with(LocalDensity.current) { SWIPE_THRESHOLD.toPx() }
    val toPrevious by rememberUpdatedState(onSwipeToPrevious)
    val toNext by rememberUpdatedState(onSwipeToNext)
    Column(
        modifier = Modifier.pointerInput(swipeThresholdPx) {
            var dragged = 0f
            detectHorizontalDragGestures(
                onDragStart = { dragged = 0f },
                onHorizontalDrag = { _, amount -> dragged += amount },
                onDragEnd = {
                    when {
                        dragged > swipeThresholdPx -> toPrevious()
                        dragged < -swipeThresholdPx -> toNext()
                    }
                },
            )
        },
    ) {
        weeks.forEach { week ->
            Row(Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
                week.forEach { cell ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        CalendarDayCell(
                            day = cell.date?.dayOfMonth,
                            state = cell.state,
                            thumbnailUrl = cell.thumbnailUrl,
                            clipCount = cell.clipCount,
                            onClick = cell.date?.let { date -> { onDayClick(date.toString()) } },
                        )
                    }
                }
            }
            Rule()
        }
    }
}

@Composable
private fun Rule() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(SetmonTheme.colors.outline),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E6, heightDp = 760)
@Composable
private fun CalendarScreenPreview() {
    val today = LocalDate.of(2026, 11, 10)
    val month = YearMonth.from(today)
    SetmonTheme {
        CalendarScreen(
            userId = 1L,
            uiState = CalendarUiState(
                month = month,
                today = today,
                weeks = buildCalendarWeeks(month, today, emptyList()),
                friends = listOf(Friend(2L, "Minjun"), Friend(3L, "Taeyang")),
            ),
            readOnly = false,
            friendNickname = null,
            onPreviousMonth = {},
            onNextMonth = {},
            onRetry = {},
            onDayClick = {},
            onCaptureClick = {},
            onFriendCalendarClick = { _, _ -> },
            onMyCalendarClick = {},
            onFriendClick = {},
            onMyPageClick = {},
        )
    }
}
