package com.setmon.core.network.auth

import com.setmon.core.network.parseErrorCode
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

private const val AUTHORIZATION = "Authorization"
private const val BEARER = "Bearer "
private const val MAX_ERROR_BODY_BYTES = 4096L

private fun Request.withBearer(accessToken: String): Request =
    newBuilder().header(AUTHORIZATION, BEARER + accessToken).build()

/**
 * 우리 서버로 가는 요청에 Access 토큰을 붙인다.
 * S3 Presigned URL 같은 외부 주소에 붙이면 업로드가 거절되므로 주소를 확인한다.
 */
internal class AuthInterceptor(
    private val storage: SessionStorage,
    private val apiBaseUrl: HttpUrl,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!needsToken(request.url)) return chain.proceed(request)
        val accessToken = runBlocking { storage.read() }?.accessToken
            ?: return chain.proceed(request)
        return chain.proceed(request.withBearer(accessToken))
    }

    private fun needsToken(url: HttpUrl): Boolean =
        url.host == apiBaseUrl.host &&
            url.port == apiBaseUrl.port &&
            !url.encodedPath.startsWith("/api/auth/")
}

/** 401 TOKEN_EXPIRED 일 때만 토큰을 새로 받아 원래 요청을 한 번 다시 보낸다 */
internal class TokenAuthenticator(
    private val refresher: TokenRefresher,
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val header = response.request.header(AUTHORIZATION) ?: return null
        if (!header.startsWith(BEARER)) return null
        if (response.priorResponse != null) return null // 재시도도 401 이면 포기

        val code = parseErrorCode(response.peekBody(MAX_ERROR_BODY_BYTES).string())
        if (code != "TOKEN_EXPIRED") return null

        val accessToken = refresher.renew(header.removePrefix(BEARER)) ?: return null
        return response.request.withBearer(accessToken)
    }
}
