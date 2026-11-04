pluginManagement {
    // 공통 설정 플러그인(setmon.android.*)을 build-logic 폴더에서 가져온다
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "setmon"

include(":app")

// core: 여러 화면이 같이 쓰는 기반
include(":core:designsystem")
include(":core:model")
include(":core:network")
include(":core:data")

// feature: 화면 묶음 (서로 의존 금지)
include(":feature:auth")
include(":feature:calendar")
include(":feature:day")
include(":feature:friend")
include(":feature:mypage")
include(":feature:capture") // B 담당
