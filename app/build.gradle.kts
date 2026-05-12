import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val localProps = Properties().also { props ->
    val f = rootProject.file("local.properties")
    if (f.exists()) props.load(f.inputStream())
}

android {
    namespace = "com.example.smartpantry"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.smartpantry"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String", "GEMINI_API_KEY",
            "\"${localProps.getProperty("GEMINI_API_KEY", "")}\""
        )
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
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.recyclerview)
    implementation(libs.swiperefreshlayout)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.runtime)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.mlkit.text.recognition)
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
    // On-device LLM inference via MediaPipe (runs Gemma locally on-device GPU)
    implementation(libs.mediapipe.tasks.genai)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
