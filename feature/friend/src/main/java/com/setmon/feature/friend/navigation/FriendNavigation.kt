package com.setmon.feature.friend.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.setmon.feature.friend.FriendScreen
import kotlinx.serialization.Serializable

/** S-07 친구 — route: friend */
@Serializable
data object FriendRoute

fun NavController.navigateToFriend(navOptions: NavOptions? = null) =
    navigate(FriendRoute, navOptions)

/**
 * @param onFriendCalendarClick 친구를 눌렀을 때 (친구 userId). app이 달력을 읽기 전용으로 연다.
 */
fun NavGraphBuilder.friendScreen(
    onFriendCalendarClick: (friendUserId: Long) -> Unit,
    onBack: () -> Unit,
) {
    composable<FriendRoute> {
        FriendScreen(onFriendCalendarClick = onFriendCalendarClick, onBack = onBack)
    }
}
