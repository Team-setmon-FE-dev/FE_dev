plugins {
    id("setmon.android.feature")
}

android {
    namespace = "com.setmon.feature.calendar"
}

dependencies {
    implementation(libs.androidx.core.ktx) // 책갈피 뒤 상태 표시줄 아이콘 색 (WindowCompat)
}
