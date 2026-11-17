package com.setmon.core.data.media

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** 갤러리 저장·공유 (B). DataModule 을 고치지 않으려고 따로 둔다 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class MediaModule {
    @Binds
    @Singleton
    abstract fun bindVlogSaver(impl: MediaStoreVlogSaver): VlogSaver
}
