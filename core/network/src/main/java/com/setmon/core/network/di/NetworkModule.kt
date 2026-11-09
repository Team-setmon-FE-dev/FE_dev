package com.setmon.core.network.di

import com.setmon.core.network.NetworkConfig
import com.setmon.core.network.ServerClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Qualifier

/** Bearer 자동 첨부·토큰 재발급이 붙은 OkHttp. 외부 주소(S3)로 보내도 토큰은 붙지 않는다 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthedOkHttp

/** 서버 주소. local.properties 값이며 끝에 / 가 없다: "$baseUrl/api/clips" */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiBaseUrl

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @AuthedOkHttp
    fun provideAuthedOkHttp(serverClient: ServerClient): OkHttpClient = serverClient.authedOkHttp

    @Provides
    @ApiBaseUrl
    fun provideApiBaseUrl(): String = NetworkConfig.BASE_URL.trimEnd('/')
}
