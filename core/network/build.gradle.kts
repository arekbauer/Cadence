import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.arekb.cadence.core.network"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 29

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Grabbing my private keys
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        localProperties.load(FileInputStream(localPropertiesFile))

        buildConfigField("String", "SPOTIFY_CLIENT_ID", "\"${localProperties.getProperty("SPOTIFY_CLIENT_ID")}\"")
        buildConfigField("String", "SPOTIFY_CLIENT_SECRET", "\"${localProperties.getProperty("SPOTIFY_CLIENT_SECRET")}\"")
        buildConfigField("String", "SPOTIFY_BASE_URL", "\"https://api.spotify.com/v1/\"")
        buildConfigField("String", "SPOTIFY_AUTH_URL", "\"https://accounts.spotify.com/\"")
    }

    buildFeatures {
        buildConfig = true
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Dependency on the Model module
    implementation(project(":core:model"))

    // Retrofit & Gson (Network)
    implementation(libs.retrofit.v2110)
    implementation(libs.converter.gson)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.logging.interceptor)

    // Paging
    implementation(libs.androidx.paging.common)
    implementation(libs.androidx.paging.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

}