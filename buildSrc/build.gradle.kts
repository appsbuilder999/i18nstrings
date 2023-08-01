plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.8.20"
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven(url = uri("../repo"))
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.3.0")
    }

}

// 引入相关的依赖
dependencies {

    implementation("com.android.tools.build:gradle:7.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
    implementation("io.ktor:ktor-client-core:2.3.1")
    implementation("io.ktor:ktor-client-cio:2.3.1")
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.cloud:google-cloud-translate:2.20.0")
    implementation("org.apache.logging.log4j:log4j-api:2.14.1")
    implementation("org.apache.logging.log4j:log4j-core:2.14.1")


    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    //implementation("com.appkim.i18nstrings.core:core:1.0.0")
}

repositories {
    google()
    mavenCentral()
}


sourceSets {
    main {
        java {
            srcDir("../plugin/src/main/kotlin")
            srcDir("../core/src/main/kotlin")
            srcDir("../core/src/plugin/kotlin")
            //exclude("**/GoogleCloudTranslateService.kt")
        }
    }
}

gradlePlugin {
    // Define the plugin
    val i18nStrings by plugins.creating {
        id = "i18nStrings"
        implementationClass = "com.appkim.i18nstrings.plugin.I18nStringsPlugin"
    }
}
