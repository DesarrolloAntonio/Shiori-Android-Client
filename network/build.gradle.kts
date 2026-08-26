plugins {
    id ("com.android.library")
}


android {
    namespace = "com.desarrollodroide.network"
    compileSdk = (findProperty("compileSdkVersion") as String).toInt()

    defaultConfig {
        minSdk = (findProperty("minSdkVersion") as String).toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        // NetworkingModule reads BuildConfig.DEBUG to pick the http log level.
        buildConfig = true
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.okhttp3.mockwebserver)


    implementation(project(":common"))
    implementation (libs.bundles.retrofit)
    implementation (libs.koin.androidx.compose)

}

// The android-junit5 plugin drove this before. It configured the test tasks through
// unitTestVariants, which AGP 9 removed, so it stopped discovering anything at all while still
// applying cleanly. Every unit test here is JUnit 5 and every instrumented test is JUnit 4, so
// plain Gradle covers it and the plugin is gone.
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
