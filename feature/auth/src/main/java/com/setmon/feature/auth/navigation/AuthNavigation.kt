package com.setmon.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.setmon.feature.auth.LoginScreen
import com.setmon.feature.auth.SignupScreen
import kotlinx.serialization.Serializable

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

fun NavGraphBuilder.loginScreen(
    onLoginSuccess: () -> Unit,
    onSignupClick: () -> Unit,
) {
    composable<LoginRoute> {
        LoginScreen(onLoginSuccess = onLoginSuccess, onSignupClick = onSignupClick)
    }
}

fun NavGraphBuilder.signupScreen(
    onSignupComplete: () -> Unit,
    onBack: () -> Unit,
) {
    composable<SignupRoute> {
        SignupScreen(onSignupComplete = onSignupComplete, onBack = onBack)
    }
}
