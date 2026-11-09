plugins {
    id("setmon.android.feature")
}

android {
    namespace = "com.setmon.feature.auth"
}

dependencies {
    // 가입 직후 권한 요청 (rememberLauncherForActivityResult)
    implementation(libs.androidx.activity.compose)
}
