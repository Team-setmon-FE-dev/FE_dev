package com.setmon.core.network.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

// 파일 이름을 바꾸면 res/xml 의 백업 제외 규칙도 같이 바꾼다
private val Context.sessionDataStore by preferencesDataStore(name = "session")

/** 로그인 상태(userId + 토큰 2개)를 기기에 저장한다. 세 값은 항상 함께 쓰고 함께 읽는다. */
@Singleton
class TokenStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    suspend fun save(tokens: AuthTokens) {
        context.sessionDataStore.edit {
            it[USER_ID] = tokens.userId
            it[ACCESS_TOKEN] = tokens.accessToken
            it[REFRESH_TOKEN] = tokens.refreshToken
        }
    }

    /** 저장된 로그인이 없으면 null */
    suspend fun read(): AuthTokens? {
        val prefs = context.sessionDataStore.data.first()
        return AuthTokens(
            userId = prefs[USER_ID] ?: return null,
            accessToken = prefs[ACCESS_TOKEN] ?: return null,
            refreshToken = prefs[REFRESH_TOKEN] ?: return null,
        )
    }

    private companion object {
        val USER_ID = longPreferencesKey("user_id")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }
}
