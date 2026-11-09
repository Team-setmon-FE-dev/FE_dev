package com.setmon.core.data.di

import com.setmon.core.data.auth.AuthRepository
import com.setmon.core.data.auth.DefaultAuthRepository
import com.setmon.core.data.calendar.CalendarRepository
import com.setmon.core.data.calendar.DefaultCalendarRepository
import com.setmon.core.data.friend.FakeFriendRepository
import com.setmon.core.data.friend.FriendRepository
import com.setmon.core.data.user.DefaultUserRepository
import com.setmon.core.data.user.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 어떤 Repository 구현을 쓸지 정하는 곳 (Mock ↔ 실서버 전환 스위치).
 * 서버 없이 화면만 돌려보려면 아래 impl 을 Fake 구현으로 바꾼다.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: DefaultAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: DefaultUserRepository): UserRepository

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(impl: DefaultCalendarRepository): CalendarRepository

    // 친구는 3주차에 서버를 연결한다
    @Binds
    @Singleton
    abstract fun bindFriendRepository(impl: FakeFriendRepository): FriendRepository
}
