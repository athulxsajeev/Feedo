plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

}

android {
    namespace = "com.example.feedo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.feedo"
        minSdk = 24
        targetSdk = 35
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0" // Jetpack Compose Compiler version
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/gradle/incremental.annotation.processors"
        }
    }
}

dependencies {
    // AndroidX Libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation (libs.rybalkinsd.kohttp)
        implementation(libs.retrofit)
        implementation(libs.converter.gson)
    implementation (libs.androidx.work.runtime.ktx)
    dependencies {
        implementation(libs.androidx.core.ktx.v1120)
        implementation(libs.androidx.lifecycle.runtime.ktx.v262)
        implementation(libs.androidx.activity.compose.v182)
        implementation(libs.androidx.lifecycle.viewmodel.compose) // For ViewModel
        implementation(libs.androidx.runtime.livedata) // For StateFlow

        // Retrofit for API calls
        implementation(libs.retrofit)
        implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    }




    // Jetpack Compose Libraries
    implementation(platform(libs.androidx.compose.bom)) // Compose BOM
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3) // Material3
    implementation ("com.google.firebase:firebase-firestore:25.1.1")

    dependencies {
        implementation ("androidx.compose.ui:ui:1.5.0" )// Update to the latest version
        implementation ("androidx.compose.material:material:1.5.0")
        implementation ("androidx.compose.ui:ui-tooling-preview:1.5.0")
        implementation ("androidx.compose.foundation:foundation:1.5.0")
    }

    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.volley)

    // Testing Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Networking (OkHttp and Gson)
    implementation(libs.okhttp)
    implementation(libs.gson)
}
