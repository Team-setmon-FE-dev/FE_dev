plugins {
    id("setmon.android.feature")
}

android {
    namespace = "com.setmon.feature.capture"
}

dependencies {
    // 내부 core 모듈 의존성
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:network"))

    // Jetpack Compose Material Icons
    implementation("androidx.compose.material:material-icons-extended:1.6.5")
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.media3.exoplayer)

    // CameraX
    val cameraxVersion = "1.3.2"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-video:$cameraxVersion")
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")

    // 네트워크 및 코루틴
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
}