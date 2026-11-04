package com.setmon.feature.day.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.setmon.feature.day.DayScreen
import kotlinx.serialization.Serializable

/**
 * S-05 하루 상세 — route: day/{userId}/{date}?readOnly=
 * @property date "yyyy-MM-dd" 형식 문자열 (예: "2026-11-04")
 */
@Serializable
data class DayRoute(
    val userId: Long,
    val date: String,
    val readOnly: Boolean = false,
)

fun NavController.navigateToDay(
    userId: Long,
    date: String,
    readOnly: Boolean = false,
    navOptions: NavOptions? = null,
) = navigate(DayRoute(userId = userId, date = date, readOnly = readOnly), navOptions)

fun NavGraphBuilder.dayScreen(
    onBack: () -> Unit,
) {
    composable<DayRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<DayRoute>()
        DayScreen(
            userId = route.userId,
            date = route.date,
            readOnly = route.readOnly,
            onBack = onBack,
        )
    }
}
