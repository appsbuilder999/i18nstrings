plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    //id("com.appkim.i18nstrings")
    id("i18nStrings")
}

android {
    namespace = "com.appkim.i18nstrings.example"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.appkim.i18nstrings.example"
        minSdk = 16
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = ("1.8")
    }
    buildFeatures {
        viewBinding = true
    }
    sourceSets {
        getByName("main") {
            res {
                srcDirs("src/main/res", "src/main/res_v2", "src/main/res_v3")
            }
        }
    }

    /**
     * debug version, because of the inclusion of google cloud translate libraries, resulting in packaging errors, so need to exclude the
     * release version, directly refer to the ui libraries, do not include the google cloud translate libraries
     */
    android.packagingOptions.resources.excludes += "META-INF/*"

}

i18nStrings {
    release = "zh-rCN, fr, es, zh-rHK"
    beta = "ja, pt, id"// , fr, de"
//    onlySupportedStringsXml = true
//    excludeXmls = "other_strings.xml"
//    betaPath = "../out/test"
//    excludeProjects = "mylibrary"
}

dependencies {
    implementation(project(":mylibrary"))
    implementation(project(":ui"))
    //implementation("com.appkim.i18nstrings:ui:0.0.12")

    implementation(libs.bundles.android)

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.6.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.6.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.2")
    testImplementation("org.mockito:mockito-core:2.23.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("com.google.truth:truth:1.1.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2022.10.00"))
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

}