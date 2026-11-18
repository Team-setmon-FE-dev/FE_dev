package com.setmon.core.network.clip

import com.setmon.core.network.ApiResult
import com.setmon.core.network.ServerClient
import com.setmon.core.network.apiCall
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.PUT
import retrofit2.http.Path
import javax.inject.Inject
import javax.inject.Singleton

internal interface ClipApi {
    /** 204. 본인 클립만. 그날 브이로그는 stale 이 된다 */
    @DELETE("api/clips/{clipId}")
    suspend fun deleteClip(@Path("clipId") clipId: Long): Response<Unit>

    /** 204. 그날 달력 칸 대표로 지정 (같은 날 다른 대표는 풀림) */
    @PUT("api/clips/{clipId}/cover")
    suspend fun setCover(@Path("clipId") clipId: Long): Response<Unit>

    /** 204. 대표 해제 → 자동(브이로그 → 첫 클립) */
    @DELETE("api/clips/{clipId}/cover")
    suspend fun clearCover(@Path("clipId") clipId: Long): Response<Unit>
}

/** 클립 삭제·대표 지정 (업로드는 B 의 capture 모듈이 따로 한다) */
@Singleton
class ClipRemoteDataSource @Inject constructor(
    private val serverClient: ServerClient,
) {
    private val api: ClipApi by lazy { serverClient.retrofit.create(ClipApi::class.java) }

    suspend fun deleteClip(clipId: Long): ApiResult<Unit> = apiCall { api.deleteClip(clipId).orThrow() }

    /** @param cover true = 대표로 지정, false = 해제 */
    suspend fun setCover(clipId: Long, cover: Boolean): ApiResult<Unit> = apiCall {
        (if (cover) api.setCover(clipId) else api.clearCover(clipId)).orThrow()
    }

    private fun Response<Unit>.orThrow() {
        if (!isSuccessful) throw HttpException(this)
    }
}
