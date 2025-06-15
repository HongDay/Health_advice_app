plugins {
    id("com.android.application") version "8.9.2"
    kotlin("android") version "1.8.22"
}

android {
    namespace = "com.example.health_advice_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.health_advice_app"
        minSdk = 28
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

    buildFeatures {
        viewBinding  = true
        dataBinding  = true
        buildConfig  = true
    }
}

dependencies {
    // AndroidX
    implementation("androidx.core:core-ktx:1.9.0")  // 선택사항이지만 권장
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // GraphView (구버전 support-v4/compat 제외)
    implementation("com.jjoe64:graphview:4.2.2") {
        exclude(group = "com.android.support", module = "support-compat")
        exclude(group = "com.android.support", module = "support-v4")
    }

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}