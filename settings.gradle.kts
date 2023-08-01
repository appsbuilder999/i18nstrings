
// settings.gradle.kts
enableFeaturePreview("VERSION_CATALOGS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = uri("../repo"))
    }

    versionCatalogs {
        create("libs") {
            library("gradle", "com.android.tools.build:gradle:7.3.0")
            library("kotlinGradlePlugin", "org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.21")

            library("ktorClientCore", "io.ktor:ktor-client-core:2.3.1")
            library("ktorClientCio", "io.ktor:ktor-client-cio:2.3.1")
            library("jsoup", "org.jsoup:jsoup:1.16.1")
            library("commonsText", "org.apache.commons:commons-text:1.10.0")
            library("gson", "com.google.code.gson:gson:2.10.1")
            library("kotlinxCoroutinesCore", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")

            library("coreKtx", "androidx.core:core-ktx:1.7.0")
            library("appcompat", "androidx.appcompat:appcompat:1.6.0")
            library("material", "com.google.android.material:material:1.5.0")


            library("googleCloudTranslate", "com.google.cloud:google-cloud-translate:2.20.0")

            bundle("core", listOf("ktorClientCore",
                "ktorClientCio",
                "jsoup",
                "commonsText",
                "gson",
                "kotlinxCoroutinesCore"))


            bundle("android", listOf("coreKtx",
                "appcompat",
                "material"))
        }
    }
}
rootProject.name = "I18nStrings"
include(":app")
include(":plugin")
include(":core")
include(":ui")
include(":mylibrary")
