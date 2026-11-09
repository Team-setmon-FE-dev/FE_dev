package com.setmon.feature.auth

/**
 * 입력 검사 규칙 (F-01). 서버도 같은 규칙으로 한 번 더 검사한다.
 * android 의존이 없어서 단위 테스트로 바로 확인할 수 있다.
 */
internal object AuthValidation {
    const val EMAIL_MAX = 100
    const val PASSWORD_MIN = 8
    const val PASSWORD_MAX = 64
    const val NICKNAME_MIN = 2
    const val NICKNAME_MAX = 10

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean = email.trim().let { it.length <= EMAIL_MAX && EMAIL_REGEX.matches(it) }

    fun isValidPassword(password: String): Boolean = password.length in PASSWORD_MIN..PASSWORD_MAX

    fun isValidNickname(nickname: String): Boolean = nickname.trim().length in NICKNAME_MIN..NICKNAME_MAX
}
