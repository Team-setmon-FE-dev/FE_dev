package com.setmon.core.data.auth

import com.setmon.core.network.ApiResult
import com.setmon.core.network.auth.AuthRemoteDataSource
import com.setmon.core.network.auth.AuthTokens
import com.setmon.core.network.auth.TokenStore
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** 실서버 구현. 성공하면 토큰을 저장한 뒤 결과를 돌려준다. */
@Singleton
class DefaultAuthRepository @Inject constructor(
    private val remote: AuthRemoteDataSource,
    private val tokenStore: TokenStore,
) : AuthRepository {

    override suspend fun savedUserId(): Long? = tokenStore.read()?.userId

    override suspend fun login(email: String, password: String): AuthResult =
        remote.login(email, password).toAuthResult()

    override suspend fun signup(email: String, password: String, nickname: String): AuthResult =
        remote.signup(email, password, nickname).toAuthResult()

    override suspend fun logout(): Boolean = try {
        tokenStore.clear()
        true
    } catch (e: IOException) {
        false
    }

    private suspend fun ApiResult<AuthTokens>.toAuthResult(): AuthResult = when (this) {
        is ApiResult.Success -> try {
            tokenStore.save(data)
            AuthResult.Success(data.userId)
        } catch (e: IOException) {
            AuthResult.Failure(AuthError.UNKNOWN) // 기기에 저장 실패
        }
        is ApiResult.ServerError -> AuthResult.Failure(authErrorOf(code))
        ApiResult.NetworkError -> AuthResult.Failure(AuthError.NETWORK)
    }
}

internal fun authErrorOf(code: String?): AuthError = when (code) {
    "EMAIL_DUPLICATED" -> AuthError.EMAIL_DUPLICATED
    "INVALID_CREDENTIALS" -> AuthError.INVALID_CREDENTIALS
    "INVALID_INPUT" -> AuthError.INVALID_INPUT
    else -> AuthError.UNKNOWN
}
