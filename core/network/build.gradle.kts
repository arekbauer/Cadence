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