package com.setmon.core.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/** 서버 호출 결과. retrofit 타입이 core:network 밖으로 나가지 않게 여기서 감싼다. */
sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>

    /** 서버가 에러로 답함. [code] 는 명세서의 에러 코드, 본문을 못 읽으면 null */
    data class ServerError(val code: String?) : ApiResult<Nothing>

    /** 인터넷 끊김, 서버 응답 없음 */
    data object NetworkError : ApiResult<Nothing>
}

/** 명세서의 공통 에러 응답: {"code": "...", "message": "..."} */
@Serializable
internal data class ErrorBody(val code: String? = null, val message: String? = null)

internal fun parseErrorCode(body: String?): String? {
    if (body.isNullOrBlank()) return null
    return try {
        ServerClient.json.decodeFromString<ErrorBody>(body).code
    } catch (e: SerializationException) {
        null
    }
}

internal suspend fun <T> apiCall(block: suspend () -> T): ApiResult<T> = try {
    ApiResult.Success(block())
} catch (e: HttpException) {
    ApiResult.ServerError(parseErrorCode(e.response()?.errorBody()?.string()))
} catch (e: SerializationException) {
    ApiResult.ServerError(code = null) // 성공 응답인데 모양이 다름
} catch (e: IOException) {
    ApiResult.NetworkError
}
