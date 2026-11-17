package com.setmon.feature.calendar.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.setmon.feature.calendar.CalendarRoot
import kotlinx.serialization.Serializable

/**
 * S-03 메인 달력 — route: calendar/{userId}?readOnly=
 * 내 달력: readOnly = false, 친구 달력: 친구 userId + readOnly = true (+ 상단 제목용 friendNickname)
 */
@Serializable
data class CalendarRoute(
    val userId: Long,
    val readOnly: Boolean = false,
    val friendNickname: String? = null,
)

fun NavController.navigateToCalendar(
    userId: Long,
    readOnly: Boolean = false,
    friendNickname: String? = null,
    navOptions: NavOptions? = null,
) = navigate(CalendarRoute(userId = userId, readOnly = readOnly, friendNickname = friendNickname), navOptions)

/**
 * @param onDayClick 날짜 칸을 눌렀을 때 (userId, "yyyy-MM-dd", readOnly)
 * @param onFriendCalendarClick 책갈피의 친구 탭을 눌렀을 때 (친구 userId, 닉네임)
 * @param onFriendClick 책갈피의 + 탭 (S-07 친구 화면)
 */
fun NavGraphBuilder.calendarScreen(
    onDayClick: (userId: Long, date: String, readOnly: Boolean) -> Unit,
    onCaptureClick: () -> Unit,
    onFriendCalendarClick: (friendUserId: Long, nickname: String) -> Unit,
    onFriendClick: () -> Unit,
    onMyPageClick: () -> Unit,
    onBack: () -> Unit,
) {
    composable<CalendarRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<CalendarRoute>()
        CalendarRoot(
            readOnly = route.readOnly,
            friendNickname = route.friendNickname,
            onDayClick = { date -> onDayClick(route.userId, date, route.readOnly) },
            onCaptureClick = onCaptureClick,
            onFriendCalendarClick = onFriendCalendarClick,
            onFriendClick = onFriendClick,
            onMyPageClick = onMyPageClick,
            onBack = onBack,
        )
    }
}
