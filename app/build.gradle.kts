plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "1.9.0" // Ensure Kotlin serialization plugin is applied
}

android {
    namespace = "com.example.asaankisaan"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.asaankisaan"
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")

    // Ktor and Kotlinx Serialization for networking
    implementation("io.ktor:ktor-client-android:2.3.9")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.9")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.9")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Google Location Services
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Jetpack Compose Navigation
    implementation(libs.androidx.navigation.compose)

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.6.0")

    // TensorFlow Lite for Android (Simplified dependencies)
//    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")
//    implementation("org.tensorflow:tensorflow-lite-gpu:2.17.0") // Commented out to avoid duplicate class issue
//    implementation("org.tensorflow:tensorflow-lite-support:0.5.0") // Explicitly re-added
//    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
//    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")

    // TensorFlow Lite Support library (for NormalizeOp, ResizeOp, TensorImage, etc.)
//    implementation("org.tensorflow:tensorflow-lite:2.12.0")
//    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
// TensorFlow Lite Task Vision library (for ImageClassifier, Category, etc.)
//    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")
// (Optional but good to have) Core TensorFlow Lite runtime

    implementation("org.tensorflow:tensorflow-lite:2.11.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
