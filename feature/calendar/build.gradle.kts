plugins {
    id("setmon.android.feature")
}

android {
    namespace = "com.setmon.feature.calendar"
}

dependencies {
    implementation(libs.androidx.core.ktx) // 책갈피 뒤 상태 표시줄 아이콘 색 (WindowCompat)
    implementation(libs.androidx.activity.compose) // 달력 사진 저장: Android 8~9 저장 권한 요청
}
