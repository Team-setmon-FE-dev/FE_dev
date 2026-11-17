package com.setmon.feature.day.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.setmon.feature.day.DayRoot
import kotlinx.serialization.Serializable

/**
 * S-05 하루 상세 — route: day/{userId}/{date}?readOnly=
 * @property date "yyyy-MM-dd" 형식 문자열 (예: "2026-11-04")
 * @property friendNickname 친구 것을 볼 때 상단 제목에 쓰는 닉네임
 */
@Serializable
data class DayRoute(
    val userId: Long,
    val date: String,
    val readOnly: Boolean = false,
    val friendNickname: String? = null,
)

fun NavController.navigateToDay(
    userId: Long,
    date: String,
    readOnly: Boolean = false,
    friendNickname: String? = null,
    navOptions: NavOptions? = null,
) = navigate(DayRoute(userId = userId, date = date, readOnly = readOnly, friendNickname = friendNickname), navOptions)

fun NavGraphBuilder.dayScreen(
    onBack: () -> Unit,
) {
    composable<DayRoute> {
        DayRoot(onBack = onBack)
    }
}
