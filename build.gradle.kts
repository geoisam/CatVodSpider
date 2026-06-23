// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.gradle.proguard) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
}

tasks.register<Delete>("cleanAll") {
    // 删除根项目构建目录
    delete(rootProject.layout.buildDirectory)
    // 删除所有子项目构建目录
    rootProject.subprojects.forEach { sub ->
        delete(sub.layout.buildDirectory)
    }
}