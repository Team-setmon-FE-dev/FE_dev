package com.setmon.core.network

import com.setmon.core.network.auth.AuthApi
import com.setmon.core.network.auth.AuthInterceptor
import com.setmon.core.network.auth.RefreshOutcome
import com.setmon.core.network.auth.RefreshRequest
import com.setmon.core.network.auth.SessionEvents
import com.setmon.core.network.auth.TokenAuthenticator
import com.setmon.core.network.auth.TokenRefresher
import com.setmon.core.network.auth.TokenStore
import com.setmon.core.network.auth.toAuthTokens
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Retrofit·OkHttp 를 한 벌만 만들어 나눠 쓴다. */
@Singleton
class ServerClient @Inject constructor(
    tokenStore: TokenStore,
    sessionEvents: SessionEvents,
) {
    /** 토큰을 붙이지 않는 기본 클라이언트. refresh 호출이 여기로 나가야 무한 반복이 없다 */
    private val plainOkHttp: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS) // 영상 업로드 고려
        .apply {
            if (BuildConfig.DEBUG) {
                // BODY 로 올리면 비밀번호와 토큰이 로그에 찍힌다.
                // network 인터셉터라서 401 → refresh → 재시도가 각각 한 줄씩 보인다.
                addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            }
        }
        .build()

    private val refreshApi: AuthApi = retrofitOf(plainOkHttp).create(AuthApi::class.java)

    private val refresher = TokenRefresher(
        storage = tokenStore,
        refreshCall = ::callRefresh,
        onSessionExpired = sessionEvents::notifyExpired,
    )

    /** Bearer 자동 첨부 + 만료 시 재발급·재시도가 되는 클라이언트. 다른 모듈에는 @AuthedOkHttp 로 준다 */
    val authedOkHttp: OkHttpClient = plainOkHttp.newBuilder()
        .addInterceptor(AuthInterceptor(tokenStore, NetworkConfig.BASE_URL.toHttpUrl()))
        .authenticator(TokenAuthenticator(refresher))
        .build()

    internal val retrofit: Retrofit = retrofitOf(authedOkHttp)

    private fun retrofitOf(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(NetworkConfig.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private fun callRefresh(refreshToken: String): RefreshOutcome = try {
        val response = refreshApi.refresh(RefreshRequest(refreshToken)).execute()
        val body = response.body()
        when {
            body != null -> RefreshOutcome.Renewed(body.toAuthTokens())
            response.code() == 401 &&
                parseErrorCode(response.errorBody()?.string()) in REFRESH_REJECTED_CODES -> RefreshOutcome.Rejected
            else -> RefreshOutcome.Unavailable // 서버 장애 등. 로그인을 지우지 않는다
        }
    } catch (e: IOException) {
        RefreshOutcome.Unavailable
    } catch (e: SerializationException) {
        RefreshOutcome.Unavailable
    }

    internal companion object {
        val json = Json {
            ignoreUnknownKeys = true // 서버가 필드를 추가해도 앱이 깨지지 않게
        }

        private val REFRESH_REJECTED_CODES = setOf("TOKEN_EXPIRED", "INVALID_TOKEN")
    }
}
