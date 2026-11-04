package com.setmon.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** app 모듈 전용: 안드로이드 앱 + Compose 기본 설정 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.configure<ApplicationExtension> {
                compileSdk {
                    version = release(SetmonConfig.COMPILE_SDK)
                }
                defaultConfig {
                    minSdk = SetmonConfig.MIN_SDK
                    targetSdk = SetmonConfig.TARGET_SDK
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }
                compileOptions {
                    sourceCompatibility = SetmonConfig.JAVA_VERSION
                    targetCompatibility = SetmonConfig.JAVA_VERSION
                }
                buildFeatures {
                    compose = true
                }
            }

            addComposeDependencies()
        }
    }
}
