plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.gradle.proguard)
}

android {
    namespace = "com.github.catvod"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.github.catvod.demo"
        minSdk = 28
        targetSdk = 37
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    packaging {
        jniLibs {
            keepDebugSymbols += "**/libquickjs-android-wrapper.so"
        }
    }

}

proguardDictionaries {
    dictionaryNames = listOf(
        "build/class-dictionary",
        "build/package-dictionary",
        "build/obfuscation-dictionary"
    )
    minLineLength = 1
    maxLineLength = 3
    linesCountInDictionary = 100000
}

dependencies {
    implementation(libs.okhttp3)
    implementation(libs.js.quickjs)
    implementation(libs.json.gson)
    implementation(libs.html.jsoup)
    implementation(libs.juniversalchardet)
    implementation(libs.orhanobut.logger)
}