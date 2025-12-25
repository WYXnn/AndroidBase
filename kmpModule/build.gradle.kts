import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
//    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.composeCompiler)
//    id("org.jetbrains.kotlin.multiplatform")
//    id("org.jetbrains.kotlin.plugin.compose")
//    id("com.android.kotlin.multiplatform.library")
//    id("com.android.lint")
    id("org.jetbrains.compose")
    kotlin("multiplatform")
}

kotlin {

    // Target declarations - add or remove as needed below. These define
    // which platforms this KMP module supports.
    // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    androidLibrary {
        namespace = "com.wyx.kmpmodule"
        compileSdk = 36
        minSdk = 24
    }


    val xcf = XCFramework("KmpModule")

    listOf(
        iosArm64(),           // 真机
        iosSimulatorArm64()   // 模拟器
    ).forEach {
        it.binaries.framework {
            baseName = "KmpModule"
            isStatic = true
            xcf.add(this) // 将此架构添加到 XCFramework
        }
    }

    // Source set declarations.
    // Declaring a target automatically creates a source set with the same name. By default, the
    // Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
    // common to share sources between related targets.
    // See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            }
        }

        androidMain {
            dependencies {
                // Add Android-specific dependencies here. Note that this source set depends on
                // commonMain by default and will correctly pull the Android artifacts of any KMP
                // dependencies declared in commonMain.
            }
        }

        iosMain {
            dependencies {
                implementation(compose.ui)
                // Add iOS-specific dependencies here. This a source set created by Kotlin Gradle
                // Plugin (KGP) that each specific iOS target (e.g., iosX64) depends on as
                // part of KMP’s default source set hierarchy. Note that this source set depends
                // on common by default and will correctly pull the iOS artifacts of any
                // KMP dependencies declared in commonMain.
            }
        }
    }

}