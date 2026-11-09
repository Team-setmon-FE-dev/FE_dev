package com.setmon.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.ui.platform.LocalDensity
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
import com.setmon.core.designsystem.component.SetmonTopBar
import com.setmon.core.designsystem.component.TodayCard
import com.setmon.core.designsystem.theme.SetmonTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle as DateTextStyle
import java.util.Locale

private val SWIPE_THRESHOLD = 64.dp

/** S-03 달력. 피그마 `S-03 · Main calendar`. 친구 탭은 친구 API 연결(3주차) 때 붙인다 */
@Composable
internal fun CalendarRoot(
    readOnly: Boolean,
    onDayClick: (date: String) -> Unit,
    onCaptureClick: () -> Unit,
    onFriendClick: () -> Unit,
    onMyPageClick: () -> Unit,
    onBack: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

    CalendarScreen(
        uiState = uiState,
        readOnly = readOnly,
        onPreviousMonth = viewModel::showPreviousMonth,
        onNextMonth = viewModel::showNextMonth,
        onRetry = viewModel::retry,
        onDayClick = onDayClick,
        onCaptureClick = onCaptureClick,
        onFriendClick = onFriendClick,
        onMyPageClick = onMyPageClick,
        onBack = onBack,
    )
}

@Composable
private fun CalendarScreen(
    uiState: CalendarUiState,
    readOnly: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onRetry: () -> Unit,
    onDayClick: (date: String) -> Unit,
    onCaptureClick: () -> Unit,
    onFriendClick: () -> Unit,
    onMyPageClick: () -> Unit,
    onBack: () -> Unit,
) {
    val colors = SetmonTheme.colors
    val shape = SetmonTheme.shapes.thumbnail
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        if (readOnly) {
            SetmonTopBar(
                title = stringResource(R.string.calendar_friend_title),
                onBack = onBack,
                readOnly = true,
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                SetmonButton(
                    text = stringResource(R.string.calendar_friends),
                    onClick = onFriendClick,
                    type = SetmonButtonType.Text,
                )
                SetmonButton(
                    text = stringResource(R.string.calendar_my_page),
                    onClick = onMyPageClick,
                    type = SetmonButtonType.Text,
                )
            }
        }

        MonthHeading(month = uiState.month, onPrevious = onPreviousMonth, onNext = onNextMonth)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SetmonTheme.spacing.screenHorizontal, vertical = 16.dp)
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
            uiState = CalendarUiState(
                month = month,
                today = today,
                weeks = buildCalendarWeeks(month, today, emptyList()),
            ),
            readOnly = false,
            onPreviousMonth = {},
            onNextMonth = {},
            onRetry = {},
            onDayClick = {},
            onCaptureClick = {},
            onFriendClick = {},
            onMyPageClick = {},
            onBack = {},
        )
    }
}
