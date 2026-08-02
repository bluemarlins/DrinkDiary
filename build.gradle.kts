// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint) apply false
}

val debugApkPath = layout.projectDirectory.file("app/build/outputs/apk/debug/app-debug.apk").asFile.absolutePath

tasks.register<Exec>("installDebugApk") {
    group = "build"
    description = "Install debug APK to a connected device via adb."
    dependsOn(":app:assembleDebug")
    commandLine("adb", "install", "-r", debugApkPath)
}

tasks.register("buildAndInstall") {
    group = "build"
    description = "Build debug APK and install it to a connected device."
    dependsOn("installDebugApk")
}
