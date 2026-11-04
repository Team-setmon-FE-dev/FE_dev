plugins {
    id("setmon.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.setmon.core.network"
}

dependencies {
    implementation(project(":core:model"))

    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
}
