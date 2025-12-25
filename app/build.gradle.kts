import org.gradle.kotlin.dsl.exclude

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.composeCompiler)
//    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("bonree")
}

android {
    namespace = "com.wyx.unicombase"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.wyx.unicombase"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    viewBinding {
        enable = true
    }


    configurations.all {
        exclude("com.intellij", "annotations")
    }
}


dependencies {

//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.appcompat)
//    implementation(libs.material)
//    implementation(libs.androidx.activity)
//    implementation(libs.androidx.constraintlayout)
    implementation(project(":commonBase"))
    implementation(project(":commonNet"))
    implementation(project(":commonLog"))
    implementation(project(":commonDns"))
    implementation(project(":oneKeyLogin"))
    implementation(project(":commonDatabase"))
    implementation(project(":commonOCR"))
    implementation(project(":commonConfig"))
    implementation(project(":commonMonitor"))
    implementation(project(":kmpModule"))

    val room_version = "2.7.0"
    api("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    api("androidx.room:room-ktx:$room_version")

    api ("com.google.dagger:hilt-android:2.57.1")
    kapt ("com.google.dagger:hilt-android-compiler:2.57.1")

    implementation(libs.androidx.activity.compose)
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(compose.components.resources)
    implementation(compose.components.uiToolingPreview)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)

}