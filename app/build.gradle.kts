plugins {
    id("setmon.android.application") // compileSdk·minSdk·Compose 등 공통 설정 (build-logic)
    id("setmon.hilt")
}

android {
    namespace = "com.setmon.app"

    defaultConfig {
        applicationId = "com.setmon.app"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
}

dependencies {
    // app은 모든 모듈을 알고, 화면들을 NavHost로 이어준다
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(project(":core:data"))

    implementation(project(":feature:auth"))
    implementation(project(":feature:calendar"))
    implementation(project(":feature:day"))
    implementation(project(":feature:friend"))
    implementation(project(":feature:mypage"))
    implementation(project(":feature:capture"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
