plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.arekb.cadence"
    compileSdk = 36

    defaultConfig {
        manifestPlaceholders += mapOf(
            "redirectSchemeName" to "cadence-app",
            "redirectHostName" to "callback",
            "redirectPathPattern" to ".*"
        )
        applicationId = "com.arekb.cadence"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))

    // Navigation & Hilt
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Feature Modules
    implementation(project(":feature:login"))
    implementation(project(":feature:home"))
    implementation(project(":feature:search"))
    implementation(project(":feature:analytics"))
    implementation(project(":feature:artist"))

    // Core Modules
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
}