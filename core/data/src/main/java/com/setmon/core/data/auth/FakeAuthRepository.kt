package com.setmon.core.data.auth

import com.setmon.core.model.MOCK_MY_USER_ID
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 서버 없이 화면을 돌려보기 위한 가짜 구현.
 *
 * 일부러 실패를 보고 싶을 때 쓰는 값
 *  - 가입: 이메일 [TAKEN_EMAIL] → 이메일 중복
 *  - 로그인: 비밀번호 [WRONG_PASSWORD] → 이메일/비밀번호 틀림
 *  - 둘 다: 이메일 [OFFLINE_EMAIL] → 네트워크 오류
 * 그 외 형식에 맞는 값은 모두 성공한다.
 */
@Singleton
class FakeAuthRepository @Inject constructor() : AuthRepository {

    /** 앱을 껐다 켜면 잊는다 → 항상 로그인 화면부터 */
    private var loggedInUserId: Long? = null

    override suspend fun savedUserId(): Long? = loggedInUserId

    override suspend fun login(email: String, password: String): AuthResult {
        delay(FAKE_LATENCY_MS)
        return when {
            email.equals(OFFLINE_EMAIL, ignoreCase = true) -> AuthResult.Failure(AuthError.NETWORK)
            password == WRONG_PASSWORD -> AuthResult.Failure(AuthError.INVALID_CREDENTIALS)
            else -> fakeSuccess()
        }
    }

    override suspend fun signup(email: String, password: String, nickname: String): AuthResult {
        delay(FAKE_LATENCY_MS)
        return when {
            email.equals(OFFLINE_EMAIL, ignoreCase = true) -> AuthResult.Failure(AuthError.NETWORK)
            email.equals(TAKEN_EMAIL, ignoreCase = true) -> AuthResult.Failure(AuthError.EMAIL_DUPLICATED)
            else -> fakeSuccess()
        }
    }

    override suspend fun logout(): Boolean {
        loggedInUserId = null
        return true
    }

    private fun fakeSuccess(): AuthResult {
        loggedInUserId = MOCK_MY_USER_ID
        return AuthResult.Success(MOCK_MY_USER_ID)
    }

    companion object {
        const val TAKEN_EMAIL = "taken@setmon.app"
        const val WRONG_PASSWORD = "wrongpassword"
        const val OFFLINE_EMAIL = "offline@setmon.app"
        private const val FAKE_LATENCY_MS = 600L
    }
}
