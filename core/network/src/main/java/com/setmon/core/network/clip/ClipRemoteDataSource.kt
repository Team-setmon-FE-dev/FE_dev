package com.setmon.core.network.clip

import com.setmon.core.network.ApiResult
import com.setmon.core.network.ServerClient
import com.setmon.core.network.apiCall
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Path
import javax.inject.Inject
import javax.inject.Singleton

internal interface ClipApi {
    /** 204. 본인 클립만. 그날 브이로그는 stale 이 된다 */
    @DELETE("api/clips/{clipId}")
    suspend fun deleteClip(@Path("clipId") clipId: Long): Response<Unit>
}

/** 클립 삭제 (업로드는 B 의 capture 모듈이 따로 한다) */
@Singleton
class ClipRemoteDataSource @Inject constructor(
    private val serverClient: ServerClient,
) {
    private val api: ClipApi by lazy { serverClient.retrofit.create(ClipApi::class.java) }

    suspend fun deleteClip(clipId: Long): ApiResult<Unit> = apiCall {
        val response = api.deleteClip(clipId)
        if (!response.isSuccessful) throw HttpException(response)
    }
}
