plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")

}

android {
    packagingOptions {
        resources {
            // Exclude duplicate META-INF/DEPENDENCIES files
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/*.kotlin_module"
        }
    }
    namespace = "com.example.welfare"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.welfare"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.2"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("com.google.maps.android:maps-compose:2.11.0")
    implementation ("androidx.compose.material:material:1.1.0")

    // CameraX
    implementation("androidx.camera:camera-core:1.4.0")
    implementation("androidx.camera:camera-lifecycle:1.4.0")
    implementation("androidx.camera:camera-view:1.4.0")
    implementation("androidx.camera:camera-extensions:1.0.0")
    implementation ("androidx.camera:camera-camera2:1.4.0")

    implementation ("com.google.guava:guava:30.1-android")
    implementation ("com.google.android.gms:play-services-auth:20.5.0")
    // Location
    implementation("com.google.android.gms:play-services-location:21.0.1")

    implementation ("com.airbnb.android:lottie-compose:6.0.0")

    implementation ("com.google.firebase:firebase-auth:23.1.0")
    implementation ("com.google.firebase:firebase-auth-interop:20.0.0")
    implementation ("com.google.firebase:firebase-firestore:25.1.1")
    implementation ("com.google.firebase:firebase-storage:21.0.1")
    // Google Cloud Vision API client library

    // Gson for JSON parsing
    implementation("com.google.code.gson:gson:2.8.9")

    // Google Maps
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")     // For HTTP requests
    implementation ("com.google.auth:google-auth-library-oauth2-http:1.19.0") // Google Auth Library
    implementation ("com.google.code.gson:gson:2.10.1")
    // AI (e.g., TensorFlow Lite or Vision API)
    implementation("org.tensorflow:tensorflow-lite:2.12.0")
    implementation ("io.coil-kt:coil-compose:2.2.2")
    implementation ("androidx.compose.ui:ui:1.5.0")
    implementation ("androidx.compose.material3:material3:1.0.0")

    implementation ("com.google.accompanist:accompanist-systemuicontroller:0.30.0")//

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}