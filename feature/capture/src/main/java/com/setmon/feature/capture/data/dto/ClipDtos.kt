package com.setmon.feature.capture.data.dto

import com.google.gson.annotations.SerializedName

// Presigned URL 요청 응답 DTO
data class PresignedUrlResponse(
    @SerializedName("video") val video: UploadTarget,
    @SerializedName("thumbnail") val thumbnail: UploadTarget,
    @SerializedName("expiresAt") val expiresAt: String
)

data class UploadTarget(
    @SerializedName("key") val key: String,
    @SerializedName("uploadUrl") val uploadUrl: String,
    @SerializedName("contentType") val contentType: String
)

// /api/clips 등록 요청 DTO
data class ClipRegisterRequest(
    @SerializedName("videoKey") val videoKey: String,
    @SerializedName("thumbKey") val thumbKey: String,
    @SerializedName("durationMs") val durationMs: Long,
    @SerializedName("recordedAt") val recordedAt: String // ISO-8601 UTC
)

// /api/clips 등록 응답 DTO
data class ClipRegisterResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("clipDate") val clipDate: String,
    @SerializedName("slotHour") val slotHour: Int,
    @SerializedName("durationMs") val durationMs: Long,
    @SerializedName("replaced") val replaced: Boolean
)