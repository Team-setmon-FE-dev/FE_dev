package com.setmon.core.data.vlog

/** 브이로그 창구. 화면(feature:day)은 이 인터페이스만 본다. */
interface VlogRepository {
    /** 오늘 올린 클립으로 브이로그를 지금 만든다 */
    suspend fun requestTodayVlog(): VlogRequestResult
}

sealed interface VlogRequestResult {
    /** 만들기 시작했거나 이미 만드는 중 */
    data object Started : VlogRequestResult

    /** 이미 최신 브이로그가 있음 */
    data object AlreadyDone : VlogRequestResult

    data class Failure(val error: VlogError) : VlogRequestResult
}

enum class VlogError { NO_CLIPS, NETWORK, UNKNOWN }
