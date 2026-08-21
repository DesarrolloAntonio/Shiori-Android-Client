plugins {
    id("com.android.library")
}

android {
    namespace = "com.desarrollodroide.domain"
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
}


dependencies {

    implementation(project(":data"))
    implementation(project(":model"))
    implementation(project(":common"))

    // coroutines
    implementation (libs.kotlinx.coroutines.android)
    implementation (libs.androidx.paging.compose)
    testImplementation (libs.kotlinx.coroutines.android)
    testImplementation (libs.kotlin.coroutines.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
}

// There is no JUnit 5 plugin: each module wires the platform itself, and a module that forgets to
// runs zero tests without saying so.
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
