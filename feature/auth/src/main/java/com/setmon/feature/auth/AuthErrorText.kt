package com.setmon.feature.auth

import androidx.annotation.StringRes
import com.setmon.core.data.auth.AuthError

/** 서버(또는 Fake)가 돌려준 에러를 화면 문구로 바꾼다. */
@StringRes
internal fun AuthError.messageRes(): Int = when (this) {
    AuthError.EMAIL_DUPLICATED -> R.string.auth_error_email_duplicated
    AuthError.INVALID_CREDENTIALS -> R.string.auth_error_invalid_credentials
    AuthError.INVALID_INPUT -> R.string.auth_error_invalid_input
    AuthError.NETWORK -> R.string.auth_error_network
    AuthError.UNKNOWN -> R.string.auth_error_unknown
}
