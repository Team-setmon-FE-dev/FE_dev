package com.setmon.core.network.auth

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

/** 로그인이 끝났음(refresh 거절)을 화면 쪽에 알린다. 받을 곳이 잠시 없어도 알림을 보관한다 */
@Singleton
class SessionEvents @Inject constructor() {
    private val channel = Channel<Unit>(Channel.CONFLATED)

    val expired: Flow<Unit> = channel.receiveAsFlow()

    internal fun notifyExpired() {
        channel.trySend(Unit)
    }
}
