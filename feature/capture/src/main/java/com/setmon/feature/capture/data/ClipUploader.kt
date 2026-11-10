package com.setmon.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class ClipUploader @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val baseUrl: String,
) {

    /**
     * S3 업로드 URL 발급 등 자사 서버 API 호출 시 주입받은 baseUrl 사용
     */
    suspend fun getUploadUrl(clipId: String): String = withContext(Dispatchers.IO) {
        val requestUrl = if (baseUrl.endsWith("/")) {
            "${baseUrl}clips/$clipId/upload-url"
        } else {
            "$baseUrl/clips/$clipId/upload-url"
        }

        val request = Request.Builder()
            .url(requestUrl)
            .get()
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Failed to get upload URL. Code: ${response.code}")
            }
            response.body?.string() ?: ""
        }
    }

    /**
     * S3 Presigned URL로 파일 업로드 (재시도 로직 포함)
     * S3 HTTP 403 등 실패 응답 수신 시에도 attempt를 증가시켜 무한 루프를 방지합니다.
     */
    suspend fun uploadToS3WithRetry(
        uploadUrl: String,
        file: File,
        contentType: String = "video/mp4",
        maxRetries: Int = 3,
    ): Boolean = withContext(Dispatchers.IO) {
        var attempt = 0
        var isSuccess = false

        while (attempt < maxRetries && !isSuccess) {
            try {
                val requestBody = file.asRequestBody(contentType.toMediaTypeOrNull())
                val request = Request.Builder()
                    .url(uploadUrl)
                    .put(requestBody)
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        isSuccess = true
                    } else {
                        // 403 등 실패 응답 시 attempt 증가
                        attempt++
                    }
                }
            } catch (e: Exception) {
                // 예외 발생 시 attempt 증가
                attempt++
            }
        }
        isSuccess
    }
}