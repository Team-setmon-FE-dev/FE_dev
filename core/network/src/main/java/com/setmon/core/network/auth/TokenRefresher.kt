package com.setmon.core.network.auth

import kotlinx.coroutines.runBlocking

internal sealed interface RefreshOutcome {
    data class Renewed(val tokens: AuthTokens) : RefreshOutcome

    /** 서버가 refresh 토큰을 거절함 (TOKEN_EXPIRED / INVALID_TOKEN) → 다시 로그인해야 한다 */
    data object Rejected : RefreshOutcome

    /** 네트워크 오류·서버 장애. 일시적일 수 있으니 저장된 로그인은 지우지 않는다 */
    data object Unavailable : RefreshOutcome
}

/**
 * 만료된 Access 토큰을 새로 받는다. OkHttp 스레드에서 동기로 불린다.
 * 요청 여러 개가 동시에 만료돼도 재발급은 한 번만 일어나도록 잠근다.
 */
internal class TokenRefresher(
    private val storage: SessionStorage,
    private val refreshCall: (refreshToken: String) -> RefreshOutcome,
    private val onSessionExpired: () -> Unit,
) {
    private val lock = Any()

    /** 재시도에 쓸 새 Access 토큰을 돌려준다. 재시도할 수 없으면 null */
    fun renew(failedAccessToken: String): String? = synchronized(lock) {
        val current = runBlocking { storage.read() } ?: return null

        // 기다리는 동안 다른 요청이 이미 재발급했다
        if (current.accessToken != failedAccessToken) return current.accessToken

        when (val outcome = refreshCall(current.refreshToken)) {
            is RefreshOutcome.Renewed -> {
                runBlocking { storage.save(outcome.tokens) }
                outcome.tokens.accessToken
            }
            RefreshOutcome.Rejected -> {
                runBlocking { storage.clear() }
                onSessionExpired()
                null
            }
            RefreshOutcome.Unavailable -> null
        }
    }
}
