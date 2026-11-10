plugins {
    // Compose·Hilt·Navigation·serialization·core(designsystem, model, data) 연결이 다 들어 있다
    id("setmon.android.feature")
}

android {
    namespace = "com.setmon.feature.capture"
}

dependencies {
    // 예외: 업로드 때문에 @AuthedOkHttp, @ApiBaseUrl 만 직접 쓴다 (docs/MODULES.md 2번). OkHttp 도 여기서 온다
    implementation(project(":core:network"))

    implementation(libs.androidx.compose.material.icons.extended)

    // 촬영
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.video)

    // 촬영 후 미리보기 반복 재생
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
}
