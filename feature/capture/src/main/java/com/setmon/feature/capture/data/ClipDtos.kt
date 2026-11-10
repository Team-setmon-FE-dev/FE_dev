package com.setmon.feature.capture.data

import kotlinx.serialization.Serializable

/** POST /api/uploads/presigned-url 응답 */
@Serializable
internal data class PresignedUrlResponse(
    val video: UploadTarget,
    val thumbnail: UploadTarget,
    val expiresAt: String? = null,
)

@Serializable
internal data class UploadTarget(
    val key: String,
    val uploadUrl: String,
    /** S3 PUT 의 Content-Type 헤더와 글자 하나까지 같아야 한다 (다르면 403) */
    val contentType: String,
)

/** POST /api/clips 요청 */
@Serializable
internal data class ClipRegisterRequest(
    val videoKey: String,
    val thumbKey: String,
    val durationMs: Int,
    /** 촬영 시각, ISO-8601 UTC. 예: 2026-11-04T05:00:03Z */
    val recordedAt: String,
)

/** POST /api/clips 응답 (201) */
@Serializable
data class ClipRegisterResponse(
    val id: Long,
    val clipDate: String,
    val slotHour: Int,
    val durationMs: Int,
    val replaced: Boolean,
)

/** GET /api/users/{userId}/days/{date} 응답 중 촬영 화면이 쓰는 부분만 */
@Serializable
internal data class DaySlotsResponse(
    val clips: List<SlotClip> = emptyList(),
)

@Serializable
internal data class SlotClip(
    val slotHour: Int,
)

/** 명세서의 공통 에러 응답: {"code": "...", "message": "..."} */
@Serializable
internal data class ErrorBody(
    val code: String? = null,
    val message: String? = null,
)
