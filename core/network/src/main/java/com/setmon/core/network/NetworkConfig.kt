package com.setmon.core.network

/**
 * core:network — 서버와 통신하는 코드(Retrofit, OkHttp, 토큰 인터셉터)가 들어갈 곳.
 * 2주차에 로그인 API를 붙이면서 채운다. feature 모듈은 이 모듈을 직접 쓰지 않고 core:data를 거친다.
 */
internal object NetworkConfig {
    // C가 서버 주소를 정하면 바꾼다
    const val BASE_URL = "https://example.invalid/"
}
