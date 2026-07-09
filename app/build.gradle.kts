plugins {
    alias(libs.plugins.android.application)
    kotlin("plugin.serialization") version "1.9.20"
}

android {
    namespace = "com.example.nova"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.nova"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            buildConfigField("String", "JELLYSEER_API_KEY", "\"${project.findProperty("jellyseer.api.key") ?: ""}\"")
        }
        debug {
            buildConfigField("String", "JELLYSEER_API_KEY", "\"${project.findProperty("jellyseer.api.key") ?: ""}\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.leanback)

    // Lifecycle & ViewModel
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime)

    // Image Loading
    implementation(libs.glide)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
}