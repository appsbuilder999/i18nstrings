plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("org.jetbrains.dokka") version "1.5.0"
    signing
}

android {
    namespace = "com.appkim.i18nstrings.ui"
    compileSdk = 33

    defaultConfig {
        minSdk = 16
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    sourceSets {
        getByName("release") {
            java {
                srcDir("../core/src/main/kotlin")

            }
        }
    }
}

dependencies {

    //implementation(libs.bundles.core)
    implementation(libs.bundles.android)
    //implementation(project(":core"))
    add("debugImplementation", project(":core"))
    add("releaseImplementation", libs.bundles.core)

    // Replace bundled strings dynamically
    implementation("dev.b3nedikt.restring:restring:5.2.2")
    // Intercept view inflation
    implementation("dev.b3nedikt.viewpump:viewpump:4.0.10")
    // Allows to update the text of views at runtime without recreating the activity
    implementation("dev.b3nedikt.reword:reword:4.0.4")
    // Manages the Locale used by the app
    implementation("dev.b3nedikt.applocale:applocale:3.1.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

val androidSourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets["main"].java.srcDirs)
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml.get().outputDirectory)
}

publishing {
    publications {
        create<MavenPublication>("i18nStringsUI") {
            groupId = "com.appkim.i18nstrings"
            artifactId = "ui"
            version = rootProject.extra["i18nVersion"] as String
            afterEvaluate {
                from(components["release"])
            }

            artifact(androidSourcesJar.get())
            artifact(javadocJar.get())

            pom {
                name.set("I18nStrings")
                description.set("I18nStrings is a turn-key Gradle plugin for Android projects, providing comprehensive internationalization and multilingual support.")
                url.set("https://github.com/appsbuilder999/i18nstrings")
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("appsbuilder999")
                        name.set("appsbuilder999")
                        email.set("appsbuilder999@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:github.com/appsbuilder999/i18nstrings.git")
                    developerConnection.set("scm:git:ssh://github.com/appsbuilder999/i18nstrings.git")
                    url.set("https://github.com/appsbuilder999/i18nstrings")
                }
            }
        }
    }

    repositories {
        maven {
            url = uri("../../repo")
        }
    }

}

signing {
    sign(publishing.publications["i18nStringsUI"])
}
