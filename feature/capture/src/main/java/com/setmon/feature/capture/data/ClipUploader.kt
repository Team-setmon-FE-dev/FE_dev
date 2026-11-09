package com.setmon.feature.capture.data

import com.google.gson.Gson
import com.setmon.feature.capture.data.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ClipUploader @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val baseUrl: String = "http://3.34.46.35:8080",
    private val gson: Gson = Gson()
) {
    /**
     * 1. Presigned URL 발급
     * 2. S3에 영상 및 썸네일 PUT 업로드 (Header Content-Type 일치 필수)
     * 3. /api/clips 에 등록 API 호출
     */
    suspend fun uploadClipProcess(
        videoFile: File,
        thumbFile: File,
        durationMs: Long,
        recordedAtEpochMs: Long = System.currentTimeMillis()
    ): Result<ClipRegisterResponse> = withContext(Dispatchers.IO) {
        try {
            // 1. Presigned URL 발급
            val presignedData = requestPresignedUrl().getOrThrow()

            // 2. S3 PUT 업로드 (자동 3회 재시도 포함)
            uploadToS3WithRetry(videoFile, presignedData.video.uploadUrl, presignedData.video.contentType).getOrThrow()
            uploadToS3WithRetry(thumbFile, presignedData.thumbnail.uploadUrl, presignedData.thumbnail.contentType).getOrThrow()

            // 3. UTC ISO-8601 포맷팅 생성 (예: 2026-11-09T08:56:00Z)
            val isoRecordedAt = DateTimeFormatter.ISO_INSTANT.format(
                Instant.ofEpochMilli(recordedAtEpochMs).atZone(ZoneOffset.UTC)
            )

            val registerReq = ClipRegisterRequest(
                videoKey = presignedData.video.key,
                thumbKey = presignedData.thumbnail.key,
                durationMs = durationMs.coerceIn(1000L, 2000L),
                recordedAt = isoRecordedAt
            )

            val response = registerClip(registerReq).getOrThrow()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun requestPresignedUrl(): Result<PresignedUrlResponse> {
        val request = Request.Builder()
            .url("$baseUrl/api/uploads/presigned-url")
            .post("".toRequestBody(null))
            .build()

        return runCatching {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw RuntimeException("Presigned URL 발급 실패 (${response.code})")
                }
                val bodyStr = response.body?.string() ?: throw RuntimeException("Empty response")
                gson.fromJson(bodyStr, PresignedUrlResponse::class.java)
            }
        }
    }

    private suspend fun uploadToS3WithRetry(
        file: File,
        uploadUrl: String,
        contentType: String,
        maxRetries: Int = 3
    ): Result<Boolean> {
        var attempt = 0
        while (attempt < maxRetries) {
            try {
                val requestBody = file.asRequestBody(contentType.toMediaTypeOrNull())
                val request = Request.Builder()
                    .url(uploadUrl)
                    .put(requestBody)
                    .header("Content-Type", contentType)
                    .build()

                val success = okHttpClient.newCall(request).execute().use { response ->
                    response.isSuccessful
                }
                if (success) return Result.success(true)
            } catch (e: Exception) {
                attempt++
                if (attempt >= maxRetries) return Result.failure(e)
                delay(1000L * attempt)
            }
        }
        return Result.failure(RuntimeException("S3 업로드 실패"))
    }

    private fun registerClip(req: ClipRegisterRequest): Result<ClipRegisterResponse> {
        val request = Request.Builder()
            .url("$baseUrl/api/clips")
            .post(gson.toJson(req).toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        return runCatching {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw RuntimeException("클립 등록 실패 (${response.code})")
                }
                val bodyStr = response.body?.string() ?: throw RuntimeException("Empty response")
                gson.fromJson(bodyStr, ClipRegisterResponse::class.java)
            }
        }
    }
}