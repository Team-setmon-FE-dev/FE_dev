package com.setmon.core.network.auth

import kotlinx.serialization.Serializable
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

internal interface AuthApi {
    @POST("api/auth/signup")
    suspend fun signup(@Body body: SignupRequest): TokenResponse

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): TokenResponse

    /** OkHttp 스레드에서 동기로 호출하므로 suspend 가 아니다 */
    @POST("api/auth/refresh")
    fun refresh(@Body body: RefreshRequest): Call<TokenResponse>
}

@Serializable
internal data class SignupRequest(val email: String, val password: String, val nickname: String)

@Serializable
internal data class LoginRequest(val email: String, val password: String)

@Serializable
internal data class RefreshRequest(val refreshToken: String)

@Serializable
internal data class TokenResponse(val userId: Long, val accessToken: String, val refreshToken: String)

internal fun TokenResponse.toAuthTokens() = AuthTokens(userId, accessToken, refreshToken)
