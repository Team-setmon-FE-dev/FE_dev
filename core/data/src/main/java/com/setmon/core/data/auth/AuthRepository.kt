package com.setmon.core.data.auth

/**
 * 로그인·회원가입 창구. 화면(feature:auth)은 이 인터페이스만 본다.
 * 실서버는 [DefaultAuthRepository], 서버 없이 돌려볼 때는 [FakeAuthRepository].
 * 바꾸는 곳은 di/DataModule.kt 한 줄뿐이다.
 */
interface AuthRepository {
    /** 기기에 저장된 로그인의 내 userId, 없으면 null. 앱 시작 시 S-00 에서 로그인 / 달력 중 어디로 갈지 정한다. */
    suspend fun savedUserId(): Long?

    suspend fun login(email: String, password: String): AuthResult

    suspend fun signup(email: String, password: String, nickname: String): AuthResult
}

sealed interface AuthResult {
    data class Success(val userId: Long) : AuthResult
    data class Failure(val error: AuthError) : AuthResult
}

/** 화면에서 구분해 보여줄 에러. 서버 code 와의 연결은 [authErrorOf] */
enum class AuthError {
    /** 가입: 이미 쓰는 이메일 (409) */
    EMAIL_DUPLICATED,

    /** 로그인: 이메일 또는 비밀번호가 틀림 (401) */
    INVALID_CREDENTIALS,

    /** 서버 입력 검사 실패 (400). 앱 검사를 통과했는데 서버가 거절한 경우 */
    INVALID_INPUT,

    /** 인터넷 끊김, 서버 응답 없음 */
    NETWORK,

    UNKNOWN,
}
