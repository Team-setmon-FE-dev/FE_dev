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
 * @param onDayClick 날짜 칸을 눌렀을 때 (userId, "yyyy-MM-dd", readOnly, 친구 닉네임 — 내 달력이면 null)
 * @param onFriendCalendarClick 책갈피의 친구 줄을 눌렀을 때 (친구 userId, 닉네임,
 *   replaceCurrent = 친구 달력에서 다른 친구로 바꾸는 중이라 지금 화면을 대신해야 함)
 * @param onMyCalendarClick 친구 달력에서 마이페이지 줄을 눌렀을 때 → 내 달력
 * @param onFriendClick 책갈피의 + 줄 (S-07 친구 화면)
 * @param onMyPageClick 내 달력에서 마이페이지 줄을 눌렀을 때 → S-12
 */
fun NavGraphBuilder.calendarScreen(
    onDayClick: (userId: Long, date: String, readOnly: Boolean, friendNickname: String?) -> Unit,
    onCaptureClick: () -> Unit,
    onFriendCalendarClick: (friendUserId: Long, nickname: String, replaceCurrent: Boolean) -> Unit,
    onMyCalendarClick: () -> Unit,
    onFriendClick: () -> Unit,
    onMyPageClick: () -> Unit,
) {
    composable<CalendarRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<CalendarRoute>()
        CalendarRoot(
            userId = route.userId,
            readOnly = route.readOnly,
            friendNickname = route.friendNickname,
            onDayClick = { date -> onDayClick(route.userId, date, route.readOnly, route.friendNickname) },
            onCaptureClick = onCaptureClick,
            onFriendCalendarClick = { friendUserId, nickname ->
                onFriendCalendarClick(friendUserId, nickname, route.readOnly)
            },
            onMyCalendarClick = onMyCalendarClick,
            onFriendClick = onFriendClick,
            onMyPageClick = onMyPageClick,
        )
    }
}
