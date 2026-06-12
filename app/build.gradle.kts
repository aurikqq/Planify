import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.aurikqq.planify"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.aurikqq.planify"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.3.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProperties = rootProject.file("local.properties")
        val props = Properties()
        if (localProperties.exists()) {
            props.load(localProperties.inputStream())
            val githubToken = props.getProperty("GITHUB_TOKEN") ?: ""
            buildConfigField("String", "GITHUB_TOKEN", "\"$githubToken\"")
        }
        else {
            buildConfigField("String", "GITHUB_TOKEN", "\"\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget("11")
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.okhttp.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.firebase.analytics)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.okhttp)
    implementation(libs.okhttp3.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.coil.compose)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.compose.material3.window.size.class1)
    implementation(libs.androidx.concurrent.futures)
    implementation(libs.androidx.concurrent.futures.ktx)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}