package com.setmon.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.setmon.core.model.MOCK_MY_USER_ID
import com.setmon.feature.auth.navigation.LoginRoute
import com.setmon.feature.auth.navigation.loginScreen
import com.setmon.feature.auth.navigation.navigateToLogin
import com.setmon.feature.auth.navigation.navigateToSignup
import com.setmon.feature.auth.navigation.signupScreen
import com.setmon.feature.calendar.navigation.calendarScreen
import com.setmon.feature.calendar.navigation.navigateToCalendar
import com.setmon.feature.capture.navigation.captureScreen
import com.setmon.feature.capture.navigation.navigateToCapture
import com.setmon.feature.day.navigation.dayScreen
import com.setmon.feature.day.navigation.navigateToDay
import com.setmon.feature.friend.navigation.friendScreen
import com.setmon.feature.friend.navigation.navigateToFriend
import com.setmon.feature.mypage.navigation.myPageScreen
import com.setmon.feature.mypage.navigation.navigateToMyPage

/**
 * 앱의 모든 화면을 이어주는 곳.
 * feature 모듈끼리는 서로를 모르기 때문에, "어느 버튼을 누르면 어디로 가는지"는 전부 여기서 정한다.
 */
@Composable
fun SetmonNavHost(
    navController: NavHostController = rememberNavController(),
) {
    // 로그인/로그아웃처럼 이전 화면을 모두 지우고 이동할 때 쓰는 옵션
    fun clearAll() = navOptions {
        popUpTo(navController.graph.id) { inclusive = true }
    }

    fun goToMyCalendar() = navController.navigateToCalendar(
        userId = MOCK_MY_USER_ID,
        readOnly = false,
        navOptions = clearAll(),
    )

    NavHost(
        navController = navController,
        startDestination = LoginRoute,
    ) {
        // S-01, S-02
        loginScreen(
            onLoginSuccess = { goToMyCalendar() },
            onSignupClick = { navController.navigateToSignup() },
        )
        signupScreen(
            onSignupComplete = { goToMyCalendar() },
            onBack = { navController.popBackStack() },
        )

        // S-03
        calendarScreen(
            onDayClick = { userId, date, readOnly ->
                navController.navigateToDay(userId = userId, date = date, readOnly = readOnly)
            },
            onCaptureClick = { navController.navigateToCapture() },
            onFriendClick = { navController.navigateToFriend() },
            onMyPageClick = { navController.navigateToMyPage() },
            onBack = { navController.popBackStack() },
        )

        // S-04 (B 담당)
        captureScreen(
            onUploadSuccess = { response ->
                navController.popBackStack()
            },
            onBack = {
                navController.popBackStack()
            }
        )

        // S-05
        dayScreen(
            onBack = { navController.popBackStack() },
        )

        // S-07
        friendScreen(
            onFriendCalendarClick = { friendUserId ->
                navController.navigateToCalendar(userId = friendUserId, readOnly = true)
            },
            onBack = { navController.popBackStack() },
        )

        // S-12
        myPageScreen(
            onLogout = { navController.navigateToLogin(navOptions = clearAll()) },
            onBack = { navController.popBackStack() },
        )
    }
}