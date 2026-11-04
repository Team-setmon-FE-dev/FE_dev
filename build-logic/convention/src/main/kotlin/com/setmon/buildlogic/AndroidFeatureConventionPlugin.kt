package com.setmon.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

/**
 * 모든 feature 모듈의 기본 설정.
 * - Compose 화면 + Hilt + type-safe 화면 이동(@Serializable route)
 * - core 모듈 3개를 자동으로 연결한다.
 * - feature끼리는 연결하지 않는다. 화면 이동은 app 모듈의 NavHost가 이어준다.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("setmon.android.library.compose")
            pluginManager.apply("setmon.hilt")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

            dependencies {
                add("implementation", project(":core:designsystem"))
                add("implementation", project(":core:model"))
                add("implementation", project(":core:data"))

                add("implementation", libs.findLibrary("androidx-navigation-compose").get())
                add("implementation", libs.findLibrary("kotlinx-serialization-json").get())
            }
        }
    }
}
