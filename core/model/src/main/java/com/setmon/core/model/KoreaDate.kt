package com.setmon.core.model

import java.time.LocalDate
import java.time.ZoneId

private val KOREA_ZONE: ZoneId = ZoneId.of("Asia/Seoul")

/** 서버가 클립 날짜를 KST 로 정하므로, 기기 시간대와 상관없이 한국 날짜를 쓴다 */
fun todayInKorea(): LocalDate = LocalDate.now(KOREA_ZONE)
