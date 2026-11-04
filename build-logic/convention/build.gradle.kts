import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.setmon.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    // ApplicationExtension, LibraryExtension 같은 AGP 타입을 쓰기 위해 필요 (실행 시에는 루트가 제공)
    compileOnly(libs.android.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "setmon.android.application"
            implementationClass = "com.setmon.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "setmon.android.library"
            implementationClass = "com.setmon.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "setmon.android.library.compose"
            implementationClass = "com.setmon.buildlogic.AndroidLibraryComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "setmon.android.feature"
            implementationClass = "com.setmon.buildlogic.AndroidFeatureConventionPlugin"
        }
        register("hilt") {
            id = "setmon.hilt"
            implementationClass = "com.setmon.buildlogic.HiltConventionPlugin"
        }
    }
}
