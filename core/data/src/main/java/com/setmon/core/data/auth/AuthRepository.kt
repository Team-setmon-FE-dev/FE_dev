package com.setmon.core.data.auth

import com.setmon.core.model.User

/**
 * 로그인·회원가입 창구. 화면(feature:auth)은 이 인터페이스만 본다.
 * 지금은 [FakeAuthRepository], 2주차에 Retrofit 구현(DefaultAuthRepository)으로 바꾼다.
 * 바꾸는 곳은 di/DataModule.kt 한 줄뿐이다.
 */
interface AuthRepository {
    /** 기기에 저장된 로그인(토큰)이 있는지. 앱 시작 시 S-00 에서 로그인 / 달력 중 어디로 갈지 정한다. */
    suspend fun hasSavedSession(): Boolean

    suspend fun login(email: String, password: String): AuthResult

    suspend fun signup(email: String, password: String, nickname: String): AuthResult
}

sealed interface AuthResult {
    data class Success(val user: User) : AuthResult
    data class Failure(val error: AuthError) : AuthResult
}

/** 서버 에러 코드와 1:1로 맞춘다. 목요일 Swagger 확정 후 이름을 C의 code 값에 맞춰 고친다. */
enum class AuthError {
    /** 가입: 이미 쓰는 이메일 (409) */
    EMAIL_DUPLICATED,

    /** 로그인: 이메일 또는 비밀번호가 틀림 (401) */
    INVALID_CREDENTIALS,

    /** 인터넷 끊김, 서버 응답 없음 */
    NETWORK,

    UNKNOWN,
}
