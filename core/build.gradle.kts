plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")

    id("maven-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

sourceSets {
    main {
        java {
            srcDir("src/plugin/kotlin")
            //exclude("**/GoogleCloudTranslateService.kt")
        }
    }
}

dependencies{
    implementation(libs.bundles.core)
    implementation(libs.googleCloudTranslate.get())

    //google cloud
//    implementation("com.google.cloud:google-cloud-translate:2.20.0")
//    {
//        exclude(group = "org.apache.httpcomponents")
//        exclude(group = "org.json", module = "json")
//    }
    //annotationProcessor("com.google.cloud:google-cloud-translate:1.12.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.2")
    testImplementation("org.mockito:mockito-core:2.23.0")
    testImplementation("com.google.truth:truth:1.1.3")
}

//tasks.withType<Test>("test") {
//    useJUnitPlatform()
//}

tasks.named<Test>("test") {
    // Use JUnit Jupiter for unit tests.
    useJUnitPlatform()
}


publishing {
    publications {
        create<MavenPublication>("i18nStringsCore") {
            groupId = "com.appkim.i18nstrings"
            artifactId = "core"
            version = "0.0.1"
            from(components["java"])
        }

    }

    repositories {
        maven {
            url = uri("$rootDir/repo")
        }
    }
}