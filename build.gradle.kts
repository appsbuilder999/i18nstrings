// Top-level build file where you can add configuration options common to all sub-projects/modules.

extra["i18nVersion"] = "0.0.8"
extra["isPublish"] = true

buildscript {
    repositories {
        google()
        mavenCentral()
        maven(url = uri("../repo"))
    }

    dependencies {
        classpath(libs.gradle.get())
        classpath(libs.kotlinGradlePlugin.get())
    }
}

plugins {
//    id("com.android.application") version "8.0.2" apply false
//    id("com.android.library") version "8.0.2" apply false
//    id("org.jetbrains.kotlin.android") version "1.8.20" apply false
    id("org.jetbrains.kotlin.jvm") version "1.8.20" apply false
    id("org.jetbrains.kotlin.android") version "1.8.20" apply false
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0-rc-1"
}

// ./gradlew plugin:publishI18nStringsPublicationToSonatypeRepository closeAndReleaseSonatypeStagingRepository
// ./gradlew ui:publishI18nStringsPublicationToSonatypeRepository closeAndReleaseSonatypeStagingRepository
//closeAndReleaseSonatypeStagingRepository need to run with publishI18nStringsPublicationToSonatypeRepository，otherwise it will fails
//https://central.sonatype.com/artifact/com.appkim.i18nstrings/ui
nexusPublishing {
    repositories {
        sonatype {  //only for users registered in Sonatype after 24 Feb 2021
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            stagingProfileId.set("2d62321aa5971f")
            username.set(System.getenv("maven_username")) // defaults to project.properties["myNexusUsername"]
            password.set(System.getenv("maven_password")) // defaults to project.properties["myNexusPassword"]
        }
    }
}


