package com.setmon.feature.mypage.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.setmon.feature.mypage.MyPageRoot
import kotlinx.serialization.Serializable

/** S-12 마이페이지 — route: mypage */
@Serializable
data object MyPageRoute

fun NavController.navigateToMyPage(navOptions: NavOptions? = null) =
    navigate(MyPageRoute, navOptions)

fun NavGraphBuilder.myPageScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit,
) {
    composable<MyPageRoute> {
        MyPageRoot(onLogout = onLogout, onBack = onBack)
    }
}
