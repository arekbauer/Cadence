import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.arekb.cadence.core.data"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 29

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Grabbing my private keys
        val localProperties = Properties()
        val localFile = rootProject.file("local.properties")
        if (localFile.exists()) {
            localProperties.load(FileInputStream(localFile))
        }

        // Helper function to get value from Property File OR Environment Variable
        fun getApiKey(propertyKey: String, envKey: String): String {
            return localProperties.getProperty(propertyKey)
                ?: System.getenv(envKey)
                ?: "MISSING_KEY" // Fallback to avoid build crash, but app might fail at runtime if key is needed
        }

        buildConfigField("String", "SPOTIFY_CLIENT_ID", "\"${getApiKey("SPOTIFY_CLIENT_ID", "SPOTIFY_CLIENT_ID")}\"")
        buildConfigField("String", "SPOTIFY_CLIENT_SECRET", "\"${getApiKey("SPOTIFY_CLIENT_SECRET", "SPOTIFY_CLIENT_SECRET")}\"")
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Modules
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Spotify
    implementation("com.spotify.android:auth:3.0.0")

    // Paging
    implementation(libs.androidx.paging.common)
    implementation(libs.retrofit.v2110)
}