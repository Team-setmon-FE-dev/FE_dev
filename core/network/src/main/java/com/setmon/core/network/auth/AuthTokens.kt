package com.setmon.core.network.auth

/** 로그인·가입·재발급이 공통으로 돌려주는 값 */
data class AuthTokens(
    val userId: Long,
    val accessToken: String,
    val refreshToken: String,
)
