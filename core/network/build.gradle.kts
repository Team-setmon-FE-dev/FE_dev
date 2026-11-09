import java.util.Properties

plugins {
    id("setmon.android.library")
    id("setmon.hilt")
    alias(libs.plugins.kotlin.serialization)
}

// 서버 주소는 각자 local.properties 에 적는다 (git 에 안 올라감). 없으면 에뮬레이터 → 내 PC localhost
val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
val apiBaseUrl: String = localProperties.getProperty("setmon.apiBaseUrl") ?: "http://10.0.2.2:8080/"

android {
    namespace = "com.setmon.core.network"

    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }
}

dependencies {
    implementation(project(":core:model"))

    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.datastore.preferences)
}
