package com.setmon.core.network.vlog

import com.setmon.core.network.ApiResult
import com.setmon.core.network.ServerClient
import com.setmon.core.network.apiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VlogRemoteDataSource @Inject constructor(
    private val serverClient: ServerClient,
) {
    private val api: VlogApi by lazy { serverClient.retrofit.create(VlogApi::class.java) }

    /** 성공하면 서버의 브이로그 상태(PENDING, PROCESSING, DONE)를 돌려준다 */
    suspend fun requestTodayVlog(): ApiResult<String> = apiCall { api.requestTodayVlog().status }
}
