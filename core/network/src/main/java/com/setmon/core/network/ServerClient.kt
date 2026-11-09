package com.setmon.core.network

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

/** Retrofit·OkHttp 를 한 벌만 만들어 나눠 쓴다. 토큰 인터셉터도 여기 [okHttpClient] 에 붙인다. */
internal object ServerClient {
    val json = Json {
        ignoreUnknownKeys = true // 서버가 필드를 추가해도 앱이 깨지지 않게
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .apply {
                if (BuildConfig.DEBUG) {
                    // BODY 로 올리면 비밀번호와 토큰이 로그에 찍힌다
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                }
            }
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(NetworkConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
}
