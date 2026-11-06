plugins {
    id("setmon.android.application") // compileSdk·minSdk·Compose 등 공통 설정 (build-logic)
    id("setmon.hilt")
}

// google-services.json(Firebase 설정 파일)은 git에 올리지 않는다. 파일이 있을 때만 Firebase 플러그인을 켜서
// 파일이 없는 팀원도 빌드는 되게 한다. 없으면 앱은 FCM 토큰 대신 "Firebase 미설정" 로그를 남긴다.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
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

    // S-00 시스템 스플래시 (Android 11 이하에서도 같은 모양)
    implementation(libs.androidx.core.splashscreen)

    // 푸시 (FCM)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
