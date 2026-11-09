package com.setmon.core.data.vlog

import com.setmon.core.network.ApiResult
import com.setmon.core.network.vlog.VlogRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultVlogRepository @Inject constructor(
    private val remote: VlogRemoteDataSource,
) : VlogRepository {

    override suspend fun requestTodayVlog(): VlogRequestResult = when (val result = remote.requestTodayVlog()) {
        is ApiResult.Success ->
            if (result.data == "DONE") VlogRequestResult.AlreadyDone else VlogRequestResult.Started
        is ApiResult.ServerError -> VlogRequestResult.Failure(
            if (result.code == "NO_CLIPS") VlogError.NO_CLIPS else VlogError.UNKNOWN,
        )
        ApiResult.NetworkError -> VlogRequestResult.Failure(VlogError.NETWORK)
    }
}
