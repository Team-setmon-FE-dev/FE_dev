package com.setmon.core.network.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TokenRefresherTest {
    private val old = AuthTokens(1L, "old-access", "old-refresh")
    private val new = AuthTokens(1L, "new-access", "new-refresh")

    private class FakeStorage(var tokens: AuthTokens?) : SessionStorage {
        override suspend fun read() = tokens
        override suspend fun save(tokens: AuthTokens) { this.tokens = tokens }
        override suspend fun clear() { tokens = null }
    }

    @Test
    fun `재발급 성공이면 토큰 2개를 교체하고 새 Access 를 준다`() {
        val storage = FakeStorage(old)
        var sentRefresh: String? = null
        val refresher = TokenRefresher(storage, { sentRefresh = it; RefreshOutcome.Renewed(new) }, {})

        assertEquals("new-access", refresher.renew("old-access"))
        assertEquals("old-refresh", sentRefresh)
        assertEquals(new, storage.tokens)
    }

    @Test
    fun `refresh 거절이면 토큰을 지우고 만료를 알린다`() {
        val storage = FakeStorage(old)
        var expired = 0
        val refresher = TokenRefresher(storage, { RefreshOutcome.Rejected }, { expired++ })

        assertNull(refresher.renew("old-access"))
        assertNull(storage.tokens)
        assertEquals(1, expired)
    }

    @Test
    fun `네트워크 오류면 로그인을 유지한다`() {
        val storage = FakeStorage(old)
        var expired = 0
        val refresher = TokenRefresher(storage, { RefreshOutcome.Unavailable }, { expired++ })

        assertNull(refresher.renew("old-access"))
        assertEquals(old, storage.tokens)
        assertEquals(0, expired)
    }

    @Test
    fun `이미 재발급됐으면 서버를 다시 부르지 않는다`() {
        var calls = 0
        val refresher = TokenRefresher(FakeStorage(new), { calls++; RefreshOutcome.Rejected }, {})

        assertEquals("new-access", refresher.renew("old-access"))
        assertEquals(0, calls)
    }

    @Test
    fun `저장된 로그인이 없으면 재시도하지 않는다`() {
        var calls = 0
        val refresher = TokenRefresher(FakeStorage(null), { calls++; RefreshOutcome.Renewed(new) }, {})

        assertNull(refresher.renew("old-access"))
        assertEquals(0, calls)
    }

    @Test
    fun `동시에 만료돼도 재발급은 한 번이다`() {
        val storage = FakeStorage(old)
        var calls = 0
        val refresher = TokenRefresher(storage, {
            calls++
            Thread.sleep(50)
            RefreshOutcome.Renewed(new)
        }, {})
        val pool = Executors.newFixedThreadPool(8)
        val start = CountDownLatch(1)
        val results = (1..8).map {
            pool.submit<String?> {
                start.await()
                refresher.renew("old-access")
            }
        }

        start.countDown()

        results.forEach { assertEquals("new-access", it.get(5, TimeUnit.SECONDS)) }
        assertEquals(1, calls)
        pool.shutdown()
    }
}
