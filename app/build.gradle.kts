plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.broker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.broker"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding=true
    }
}

dependencies {
    // Ktor Dependencies (Remove redundant versions)
    implementation("io.ktor:ktor-client-core:2.3.0") // Core Ktor Client
    implementation("io.ktor:ktor-client-cio:2.3.0") // CIO engine (to be used in HttpClient)
    implementation("io.ktor:ktor-client-json:2.3.0") // JSON feature for Ktor
    implementation("io.ktor:ktor-client-serialization:2.3.0") // For Kotlin serialization support

    // Coroutine Dependencies
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1") // For background coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1") // For Android-specific coroutine handling

    // Retrofit (if used in your project)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // For JSON conversion with Gson

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.15.1") // Use the latest Glide version
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1") // If you're using Glide with annotation processor

    // Material Design (ensure the latest version)
    implementation("com.google.android.material:material:1.9.0")

    // AndroidX Dependencies (for ViewBinding and other components)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Test Dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
