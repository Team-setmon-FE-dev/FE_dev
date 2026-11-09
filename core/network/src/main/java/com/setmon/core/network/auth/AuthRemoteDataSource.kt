package com.setmon.core.network.auth

import com.setmon.core.network.ApiResult
import com.setmon.core.network.ServerClient
import com.setmon.core.network.apiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRemoteDataSource @Inject constructor(
    private val serverClient: ServerClient,
) {
    private val api: AuthApi by lazy { serverClient.retrofit.create(AuthApi::class.java) }

    suspend fun login(email: String, password: String): ApiResult<AuthTokens> =
        apiCall { api.login(LoginRequest(email, password)).toAuthTokens() }

    suspend fun signup(email: String, password: String, nickname: String): ApiResult<AuthTokens> =
        apiCall { api.signup(SignupRequest(email, password, nickname)).toAuthTokens() }
}
