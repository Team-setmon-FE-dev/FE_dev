package com.setmon.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.setmon.feature.auth.login.LoginRoot
import com.setmon.feature.auth.signup.SignupRoot
import com.setmon.feature.auth.splash.SplashRoot
import kotlinx.serialization.Serializable

/** S-00 스플래시(브랜드 인트로) — 앱의 첫 화면 */
@Serializable
data object SplashRoute

/** S-01 로그인 — route: login */
@Serializable
data object LoginRoute

/** S-02 회원가입 — route: signup */
@Serializable
data object SignupRoute

fun NavController.navigateToLogin(navOptions: NavOptions? = null) =
    navigate(LoginRoute, navOptions)

fun NavController.navigateToSignup(navOptions: NavOptions? = null) =
    navigate(SignupRoute, navOptions)

/** 인트로를 잠깐 보여준 뒤 저장된 로그인이 없으면 [onNeedLogin], 있으면 [onLoggedIn] */
fun NavGraphBuilder.splashScreen(
    onNeedLogin: () -> Unit,
    onLoggedIn: () -> Unit,
) {
    composable<SplashRoute> {
        SplashRoot(onNeedLogin = onNeedLogin, onLoggedIn = onLoggedIn)
    }
}

fun NavGraphBuilder.loginScreen(
    onLoginSuccess: () -> Unit,
    onSignupClick: () -> Unit,
) {
    composable<LoginRoute> {
        LoginRoot(onLoginSuccess = onLoginSuccess, onSignupClick = onSignupClick)
    }
}

/** 가입 성공 → 권한 안내 → [onSignupComplete]. 권한 안내는 같은 route 안의 두 번째 단계다. */
fun NavGraphBuilder.signupScreen(
    onSignupComplete: () -> Unit,
    onBack: () -> Unit,
) {
    composable<SignupRoute> {
        SignupRoot(onSignupComplete = onSignupComplete, onBack = onBack)
    }
}
