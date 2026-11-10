package com.setmon.core.network.di

import com.setmon.core.network.ClipUploader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CaptureModule {

    @Provides
    @Singleton
    fun provideClipUploader(
        @AuthedOkHttp okHttpClient: OkHttpClient,
        @ApiBaseUrl baseUrl: String,
    ): ClipUploader {
        return ClipUploader(
            okHttpClient = okHttpClient,
            baseUrl = baseUrl,
        )
    }
}