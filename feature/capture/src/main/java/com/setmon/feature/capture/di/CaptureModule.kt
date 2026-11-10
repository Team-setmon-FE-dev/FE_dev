package com.setmon.feature.capture.di

import com.setmon.core.network.di.ApiBaseUrl
import com.setmon.core.network.di.AuthedOkHttp
import com.setmon.feature.capture.data.ClipUploader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

/** core:network 에서는 @AuthedOkHttp, @ApiBaseUrl 두 개만 가져다 쓴다 (docs/MODULES.md 2번) */
@Module
@InstallIn(SingletonComponent::class)
internal object CaptureModule {

    @Provides
    @Singleton
    fun provideClipUploader(
        @AuthedOkHttp okHttpClient: OkHttpClient,
        @ApiBaseUrl baseUrl: String,
    ): ClipUploader = ClipUploader(okHttpClient = okHttpClient, baseUrl = baseUrl)
}
