package com.setmon.core.data.day

import com.setmon.core.model.DayDetail
import java.time.LocalDate

/** 하루 상세 창구. 화면(feature:day)은 이 인터페이스만 본다. */
interface DayRepository {
    suspend fun getDay(userId: Long, date: LocalDate): DayResult

    /** 성공(이미 지워진 경우 포함)이면 null */
    suspend fun deleteClip(clipId: Long): DayError?
}

sealed interface DayResult {
    data class Success(val day: DayDetail) : DayResult
    data class Failure(val error: DayError) : DayResult
}

enum class DayError {
    NETWORK,

    /** 친구가 아니게 돼서 볼 수 없음 (403 FORBIDDEN) */
    FORBIDDEN,
    UNKNOWN,
}
