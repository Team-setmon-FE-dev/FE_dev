package com.setmon.feature.capture.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 업로드 실패.
 * @param code 명세서의 에러 코드(PAST_SLOT 등). 서버가 답하지 못했으면 null
 * @param retryable 같은 파일로 다시 시도하면 될 수 있는 실패인지
 */
class ClipUploadException(
    val code: String?,
    message: String,
    val retryable: Boolean,
    cause: Throwable? = null,
) : Exception(message, cause)

/**
 * "파일을 넣으면 업로드 후 결과를 돌려주는" 함수 하나.
 *   1. POST /api/uploads/presigned-url  (영상·썸네일 URL 과 키)
 *   2. 각 uploadUrl 에 PUT              (Content-Type 은 응답의 contentType 그대로)
 *   3. POST /api/clips                  (키, 길이, 촬영 시각)
 *
 * @param okHttpClient core:network 의 @AuthedOkHttp. 우리 서버에는 Bearer 가 붙고 S3 주소에는 붙지 않는다
 * @param baseUrl core:network 의 @ApiBaseUrl. 끝에 / 가 없다
 */
class ClipUploader(
    private val okHttpClient: OkHttpClient,
    baseUrl: String,
) {
    private val baseUrl = baseUrl.trimEnd('/')
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * 실패하면 자동으로 [MAX_ATTEMPTS]번까지 다시 시도한다.
     * 다시 해도 소용없는 실패(PAST_SLOT, CLIP_TOO_LONG 등)는 바로 돌려준다.
     *
     * @param recordedAt 녹화를 시작한 시각 (업로드 시각이 아니다. 서버가 이 값으로 시간 슬롯을 정한다)
     * @param onAttempt 몇 번째 시도인지 (1부터)
     */
    suspend fun upload(
        videoFile: File,
        thumbFile: File,
        durationMs: Int,
        recordedAt: Instant,
        onAttempt: (attempt: Int) -> Unit = {},
    ): Result<ClipRegisterResponse> = withContext(Dispatchers.IO) {
        var last: ClipUploadException? = null
        for (attempt in 1..MAX_ATTEMPTS) {
            onAttempt(attempt)
            try {
                return@withContext Result.success(uploadOnce(videoFile, thumbFile, durationMs, recordedAt))
            } catch (e: ClipUploadException) {
                last = e
                if (!e.retryable) break
            }
            if (attempt < MAX_ATTEMPTS) delay(attempt * 1000L)
        }
        Result.failure(last ?: ClipUploadException(null, "업로드에 실패했습니다.", retryable = true))
    }

    /**
     * 그 날짜·시간 슬롯에 이미 올린 클립이 있는지. 있으면 다음 업로드가 그 클립을 교체한다.
     * 조회에 실패하면 null (안내 문구만 못 띄울 뿐 촬영은 막지 않는다)
     *
     * @param date KST 날짜, @param slotHour KST 0~23
     */
    suspend fun isSlotOccupied(userId: Long, date: LocalDate, slotHour: Int): Boolean? =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("$baseUrl/api/users/$userId/days/$date")
                .get()
                .build()
            try {
                callApi<DaySlotsResponse>(request).clips.any { it.slotHour == slotHour }
            } catch (e: ClipUploadException) {
                null
            }
        }

    private fun uploadOnce(
        videoFile: File,
        thumbFile: File,
        durationMs: Int,
        recordedAt: Instant,
    ): ClipRegisterResponse {
        // presigned URL 은 10분만 유효하므로 시도할 때마다 새로 받는다
        val urls = issuePresignedUrls()
        putToS3(urls.video, videoFile)
        putToS3(urls.thumbnail, thumbFile)
        return registerClip(
            ClipRegisterRequest(
                videoKey = urls.video.key,
                thumbKey = urls.thumbnail.key,
                durationMs = durationMs,
                recordedAt = recordedAt.truncatedTo(ChronoUnit.SECONDS).toString(),
            ),
        )
    }

    private fun issuePresignedUrls(): PresignedUrlResponse {
        val request = Request.Builder()
            .url("$baseUrl/api/uploads/presigned-url")
            .post(EMPTY_BODY) // 요청 본문 없음
            .build()
        return callApi(request)
    }

    private fun putToS3(target: UploadTarget, file: File) {
        val request = Request.Builder()
            .url(target.uploadUrl)
            .put(file.asRequestBody(target.contentType.toMediaType()))
            .build()
        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    // 403: URL 만료 또는 Content-Type 불일치. 새 URL 로 다시 시도한다
                    throw ClipUploadException(null, "파일 업로드에 실패했습니다. (${response.code})", retryable = true)
                }
            }
        } catch (e: IOException) {
            throw ClipUploadException(null, "네트워크 연결을 확인해주세요.", retryable = true, cause = e)
        }
    }

    private fun registerClip(body: ClipRegisterRequest): ClipRegisterResponse {
        val request = Request.Builder()
            .url("$baseUrl/api/clips")
            .post(json.encodeToString(ClipRegisterRequest.serializer(), body).toRequestBody(JSON))
            .build()
        return callApi(request)
    }

    /** 우리 서버 호출. 에러면 명세서의 {code, message} 를 읽어 [ClipUploadException] 으로 바꾼다 */
    private inline fun <reified T> callApi(request: Request): T {
        try {
            okHttpClient.newCall(request).execute().use { response ->
                val text = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    return json.decodeFromString<T>(text)
                }
                val error = try {
                    json.decodeFromString<ErrorBody>(text)
                } catch (e: SerializationException) {
                    ErrorBody()
                }
                throw ClipUploadException(
                    code = error.code,
                    message = error.message ?: "서버 오류가 발생했습니다. (${response.code})",
                    // 서버 장애(5xx)와 동시 업로드 충돌만 다시 시도한다
                    retryable = response.code >= 500 || error.code == "CLIP_UPLOAD_CONFLICT",
                )
            }
        } catch (e: IOException) {
            throw ClipUploadException(null, "네트워크 연결을 확인해주세요.", retryable = true, cause = e)
        } catch (e: SerializationException) {
            throw ClipUploadException(null, "서버 응답을 읽지 못했습니다.", retryable = false, cause = e)
        }
    }

    private companion object {
        const val MAX_ATTEMPTS = 3
        val JSON = "application/json".toMediaType()
        val EMPTY_BODY: RequestBody = ByteArray(0).toRequestBody(null)
    }
}
