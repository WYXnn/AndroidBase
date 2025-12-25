// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.hilt) apply false
    id("com.google.devtools.ksp") version "2.2.21-2.0.4" apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.androidLint) apply false

}

buildscript {
    dependencies {
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")
        classpath ("com.bonree.agent.android:bonree:8.21.0")
        classpath ("com.github.aasitnikov:fat-aar-android:1.4.4")
    }
}