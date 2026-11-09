package com.setmon.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.setmon.feature.auth.navigation.SplashRoute
import com.setmon.feature.auth.navigation.loginScreen
import com.setmon.feature.auth.navigation.navigateToLogin
import com.setmon.feature.auth.navigation.navigateToSignup
import com.setmon.feature.auth.navigation.signupScreen
import com.setmon.feature.auth.navigation.splashScreen
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * 앱의 모든 화면을 이어주는 곳.
 * feature 모듈끼리는 서로를 모르기 때문에, "어느 버튼을 누르면 어디로 가는지"는 전부 여기서 정한다.
 */
@Composable
fun SetmonNavHost(
    navController: NavHostController = rememberNavController(),
    /** refresh 가 거절돼 다시 로그인해야 할 때 값이 온다 */
    sessionExpired: Flow<Unit> = emptyFlow(),
) {
    // 로그인/로그아웃처럼 이전 화면을 모두 지우고 이동할 때 쓰는 옵션
    fun clearAll() = navOptions {
        popUpTo(navController.graph.id) { inclusive = true }
    }

    fun goToMyCalendar(userId: Long) = navController.navigateToCalendar(
        userId = userId,
        readOnly = false,
        navOptions = clearAll(),
    )

    LaunchedEffect(navController, sessionExpired) {
        sessionExpired.collect { navController.navigateToLogin(navOptions = clearAll()) }
    }

    NavHost(
        navController = navController,
        startDestination = SplashRoute,
    ) {
        // S-00 → 로그인 기록이 없으면 S-01, 있으면 S-03 (인트로는 뒤로 가기로 돌아오지 않게 지운다)
        splashScreen(
            onNeedLogin = { navController.navigateToLogin(navOptions = clearAll()) },
            onLoggedIn = { userId -> goToMyCalendar(userId) },
        )

        // S-01, S-02
        loginScreen(
            onLoginSuccess = { userId -> goToMyCalendar(userId) },
            onSignupClick = { navController.navigateToSignup() },
        )
        signupScreen(
            onSignupComplete = { userId -> goToMyCalendar(userId) },
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
            // TODO(2주차): 달력으로 돌아간 뒤 오늘 칸 새로고침
            onUploadComplete = { navController.popBackStack() },
            onBack = { navController.popBackStack() },
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
