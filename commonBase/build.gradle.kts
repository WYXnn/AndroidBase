plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.wyx.examplebase"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    viewBinding {
        enable = true
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

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

apply(from = "../publish-config.gradle.kts")

dependencies {

    api("androidx.core:core-ktx:1.17.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    api("androidx.fragment:fragment-ktx:1.6.2")
    api("androidx.appcompat:appcompat:1.7.1")
    api("com.google.android.material:material:1.13.0")
    api("androidx.activity:activity:1.10.1")
    api("androidx.constraintlayout:constraintlayout:2.2.1")
    api("junit:junit:4.13.2")
    api("androidx.test.ext:junit:1.2.1")
    api("androidx.test.espresso:espresso-core:3.6.1")


    api("org.greenrobot:eventbus:3.3.1")

    api ("com.google.dagger:hilt-android:2.57.1")
    kapt ("com.google.dagger:hilt-android-compiler:2.57.1")

    api("com.squareup.okhttp3:okhttp:5.1.0")
    api("com.squareup.okhttp3:logging-interceptor:5.1.0")
    api("com.squareup.retrofit2:retrofit:3.0.0")
    api("com.squareup.retrofit2:converter-gson:3.0.0")

    api("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")

    api("io.coil-kt.coil3:coil:3.3.0")
    api("io.coil-kt.coil3:coil-network-okhttp:3.3.0")
    api("io.coil-kt.coil3:coil-gif:3.3.0")
    api("io.coil-kt.coil3:coil-video:3.3.0")

    api("com.tencent:mmkv-static:1.2.9")

    api("com.guolindev.permissionx:permissionx:1.8.1")

}