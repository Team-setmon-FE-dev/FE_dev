plugins {
    id("setmon.android.library.compose")
}

android {
    namespace = "com.setmon.core.designsystem"
}

dependencies {
    implementation(libs.coil.compose)
}
