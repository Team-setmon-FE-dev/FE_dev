package com.setmon.core.network.auth

/** 토큰 갱신에 필요한 저장 기능. 테스트에서 기기 저장소 없이 돌리려고 분리했다 */
interface SessionStorage {
    suspend fun read(): AuthTokens?
    suspend fun save(tokens: AuthTokens)
    suspend fun clear()
}
