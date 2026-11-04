package com.setmon.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** 화면이 없는 라이브러리 모듈(core:model, core:network, core:data)의 기본 설정 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")

            extensions.configure<LibraryExtension> {
                compileSdk {
                    version = release(SetmonConfig.COMPILE_SDK)
                }
                defaultConfig {
                    minSdk = SetmonConfig.MIN_SDK
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }
                compileOptions {
                    sourceCompatibility = SetmonConfig.JAVA_VERSION
                    targetCompatibility = SetmonConfig.JAVA_VERSION
                }
            }
        }
    }
}
