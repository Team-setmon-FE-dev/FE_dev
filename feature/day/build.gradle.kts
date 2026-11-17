plugins {
    id("setmon.android.feature")
}

android {
    namespace = "com.setmon.feature.day"
}

dependencies {
    // S-05 플레이어 (B 의 capture 와 같은 버전 — media3 모듈끼리는 버전이 같아야 한다)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.coil.compose) // 재생 전 썸네일·타임라인 칸
    implementation(libs.androidx.activity.compose) // Android 8~9 저장 권한 요청
}
