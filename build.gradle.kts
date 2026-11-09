// Top-level build file where you can add configuration options common to all sub-projects/modules.
// 여기서는 플러그인 "버전만" 등록하고(apply false), 실제 적용은 각 모듈이 build-logic의 공통 플러그인으로 한다.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.google.services) apply false
}
