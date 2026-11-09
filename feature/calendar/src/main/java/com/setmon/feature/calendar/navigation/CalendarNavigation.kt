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
 * 내 달력: readOnly = false, 친구 달력: 친구 userId + readOnly = true
 */
@Serializable
data class CalendarRoute(
    val userId: Long,
    val readOnly: Boolean = false,
)

fun NavController.navigateToCalendar(
    userId: Long,
    readOnly: Boolean = false,
    navOptions: NavOptions? = null,
) = navigate(CalendarRoute(userId = userId, readOnly = readOnly), navOptions)

/**
 * @param onDayClick 날짜 칸을 눌렀을 때 (userId, "yyyy-MM-dd", readOnly)
 */
fun NavGraphBuilder.calendarScreen(
    onDayClick: (userId: Long, date: String, readOnly: Boolean) -> Unit,
    onCaptureClick: () -> Unit,
    onFriendClick: () -> Unit,
    onMyPageClick: () -> Unit,
    onBack: () -> Unit,
) {
    composable<CalendarRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<CalendarRoute>()
        CalendarRoot(
            readOnly = route.readOnly,
            onDayClick = { date -> onDayClick(route.userId, date, route.readOnly) },
            onCaptureClick = onCaptureClick,
            onFriendClick = onFriendClick,
            onMyPageClick = onMyPageClick,
            onBack = onBack,
        )
    }
}
