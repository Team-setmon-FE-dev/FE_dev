package com.setmon.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.setmon.app.push.PushTarget
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    /** 알림을 눌러 들어왔을 때 열 화면 (없으면 null) */
    pendingPush: StateFlow<PushTarget?> = MutableStateFlow(null),
    /** [pendingPush] 를 열었으니 비우라는 신호 */
    onPushHandled: () -> Unit = {},
    /** 로그인·가입·자동 로그인으로 내 달력에 들어갈 때마다. 기기 토큰을 서버에 등록한다 */
    onSignedIn: () -> Unit = {},
) {
    // 로그인한 내 userId. 로그인 전·로그아웃 후에는 null 이라 알림 화면은 로그인할 때까지 기다린다
    var myUserId by rememberSaveable { mutableStateOf<Long?>(null) }
    val push by pendingPush.collectAsState()

    // 로그인/로그아웃처럼 이전 화면을 모두 지우고 이동할 때 쓰는 옵션
    fun clearAll() = navOptions {
        popUpTo(navController.graph.id) { inclusive = true }
    }

    fun goToMyCalendar(userId: Long) {
        navController.navigateToCalendar(
            userId = userId,
            readOnly = false,
            navOptions = clearAll(),
        )
        myUserId = userId
        onSignedIn()
    }

    fun goToLogin() {
        myUserId = null
        navController.navigateToLogin(navOptions = clearAll())
    }

    LaunchedEffect(navController, sessionExpired) {
        sessionExpired.collect { goToLogin() }
    }

    // 알림 탭: 내 달력 위에 촬영 / 그날 하루 상세를 연다 (뒤로 가기 하면 달력).
    // 앱이 꺼져 있었으면 스플래시가 자동 로그인으로 달력에 들어간 뒤(myUserId 가 생긴 뒤) 연다.
    // 로그인이 안 돼 있으면 기다렸다가 로그인하고 달력에 들어가면 연다.
    LaunchedEffect(push, myUserId) {
        val target = push ?: return@LaunchedEffect
        val userId = myUserId ?: return@LaunchedEffect
        // 같은 화면을 보고 있을 때 또 누르면 겹쳐 쌓지 않는다
        val singleTop = navOptions { launchSingleTop = true }
        when (target) {
            PushTarget.Capture -> navController.navigateToCapture(navOptions = singleTop)
            is PushTarget.Day -> navController.navigateToDay(
                userId = userId,
                date = target.date,
                readOnly = false,
                navOptions = singleTop,
            )
        }
        onPushHandled()
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
            onFriendCalendarClick = { friendUserId, nickname ->
                navController.navigateToCalendar(userId = friendUserId, readOnly = true, friendNickname = nickname)
            },
            onFriendClick = { navController.navigateToFriend() },
            onMyPageClick = { navController.navigateToMyPage() },
            onBack = { navController.popBackStack() },
        )

        // S-04 (B 담당)
        captureScreen(
            onUploadComplete = { navController.popBackStack() },
            onBack = { navController.popBackStack() },
        )

        // S-05
        dayScreen(
            onBack = { navController.popBackStack() },
        )

        // S-07
        friendScreen(
            onFriendCalendarClick = { friendUserId, nickname ->
                navController.navigateToCalendar(userId = friendUserId, readOnly = true, friendNickname = nickname)
            },
            onBack = { navController.popBackStack() },
        )

        // S-12
        myPageScreen(
            onLogout = { goToLogin() },
            onBack = { navController.popBackStack() },
        )
    }
}