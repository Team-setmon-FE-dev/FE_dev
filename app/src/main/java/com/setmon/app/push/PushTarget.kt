package com.setmon.app.push

import android.content.Intent

/**
 * 알림을 눌렀을 때 열 화면. 서버 형식은 BE_dev docs/FCM.md
 * - CAPTURE → S-04 촬영 (route capture)
 * - VLOG_DONE → 그날 내 S-05 (route day/{내 userId}/{date})
 */
sealed interface PushTarget {
    data object Capture : PushTarget

    /** @param date yyyy-MM-dd (KST) */
    data class Day(val date: String) : PushTarget

    companion object {
        const val EXTRA_TYPE = "setmon.push.type"
        const val EXTRA_DATE = "setmon.push.date"

        const val TYPE_CAPTURE = "CAPTURE"
        const val TYPE_VLOG_DONE = "VLOG_DONE"

        private val DATE_PATTERN = Regex("""\d{4}-\d{2}-\d{2}""")

        /** FCM data(type, date) → 화면. 모르는 type 이나 날짜 모양이 이상하면 null (무시한다) */
        fun of(type: String?, date: String?): PushTarget? = when (type) {
            TYPE_CAPTURE -> Capture
            TYPE_VLOG_DONE -> date?.takeIf { DATE_PATTERN.matches(it) }?.let(::Day)
            else -> null
        }

        /**
         * 알림 탭으로 열린 Intent 에서 꺼낸다. 알림에서 온 게 아니면 null.
         * 앱이 만든 알림은 EXTRA_*, Firebase 콘솔 테스트 메시지(알림 + 맞춤 데이터)는 시스템이 data 를
         * "type"·"date" 그대로 extras 에 넣어 주므로 둘 다 본다.
         */
        fun from(intent: Intent?): PushTarget? = intent?.let {
            of(
                type = it.getStringExtra(EXTRA_TYPE) ?: it.getStringExtra("type"),
                date = it.getStringExtra(EXTRA_DATE) ?: it.getStringExtra("date"),
            )
        }
    }
}
