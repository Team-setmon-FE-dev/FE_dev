package com.setmon.core.network

/**
 * 서버 주소. 값은 프로젝트 루트의 local.properties 에서 온다 (core/network/build.gradle.kts).
 *   setmon.apiBaseUrl=http://서버주소:8080/
 * 없으면 에뮬레이터에서 내 PC의 localhost 를 가리키는 10.0.2.2:8080 을 쓴다.
 */
internal object NetworkConfig {
    /** Retrofit 은 주소가 / 로 끝나야 한다 */
    val BASE_URL: String = BuildConfig.API_BASE_URL.trimEnd('/') + "/"
}
