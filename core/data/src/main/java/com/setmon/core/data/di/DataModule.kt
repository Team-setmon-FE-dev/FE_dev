package com.setmon.core.data.di

import com.setmon.core.data.auth.AuthRepository
import com.setmon.core.data.auth.FakeAuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 어떤 Repository 구현을 쓸지 정하는 곳 (Mock ↔ 실서버 전환 스위치).
 * 2주차: FakeAuthRepository → DefaultAuthRepository 로 바꾸면 화면 코드는 그대로 실서버에 붙는다.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FakeAuthRepository): AuthRepository
}
