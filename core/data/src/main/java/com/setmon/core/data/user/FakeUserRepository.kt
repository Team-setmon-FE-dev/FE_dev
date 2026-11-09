package com.setmon.core.data.user

import com.setmon.core.model.MOCK_MY_USER_ID
import com.setmon.core.model.MyProfile
import javax.inject.Inject
import javax.inject.Singleton

/** 서버 없이 S-12 를 돌려보기 위한 가짜 구현 */
@Singleton
class FakeUserRepository @Inject constructor() : UserRepository {
    override suspend fun getMyProfile(): MyProfileResult = MyProfileResult.Success(
        MyProfile(
            id = MOCK_MY_USER_ID,
            email = "yeeun@setmon.app",
            nickname = "Yeeun",
            friendCode = "K7Q2MX",
            totalRecordDays = 12,
        ),
    )
}
