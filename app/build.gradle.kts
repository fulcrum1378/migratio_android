plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "ir.mahdiparastesh.migratio"
    compileSdk = 35
    buildToolsVersion = System.getenv("ANDROID_BUILD_TOOLS_VERSION")

    defaultConfig {
        applicationId = "ir.mahdiparastesh.migratio"
        minSdk = 24
        targetSdk = 35
        versionCode = 9
        versionName = "4.0.9"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_22; targetCompatibility = JavaVersion.VERSION_22
    }
    kotlinOptions { jvmTarget = "22" }
    buildFeatures { viewBinding = true }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    val roomVersion = "2.6.1"

    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("com.airbnb.android:lottie:6.2.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
}
