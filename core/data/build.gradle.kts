plugins {
    id("setmon.android.library")
    id("setmon.hilt")
}

android {
    namespace = "com.setmon.core.data"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:network"))
}
