pluginManagement {
    repositories {
        maven {
            setUrl("https://jitpack.io")
            content {
                includeGroup("com.github.aasitnikov")
            }
        }
        maven { setUrl("https://gitlab.bonree.com/BonreeSDK_TAPM/Android/raw/master") }
        maven { setUrl("https://maven.aliyun.com/repository/public/") }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        maven {
            setUrl("https://jitpack.io")
            content {
                includeGroup("com.github.aasitnikov")
            }
        }
        maven { setUrl("https://gitlab.bonree.com/BonreeSDK_TAPM/Android/raw/master") }
        maven { setUrl("https://maven.aliyun.com/repository/public/") }
        google()
        mavenCentral()
    }
}

rootProject.name = "UnicomBase"
include(":app")
include(":commonBase")
include(":commonNet")
include(":commonDatabase")
include(":oneKeyLogin")
include(":commonMonitor")
include(":commonOCR")
include(":commonDns")
include(":commonLog")
include(":commonConfig")
include(":kmpModule")
include(":shared")
include(":commonConfigApollo")
include(":commonNetKtor")
