plugins {
    id ("com.android.library")
    id ("org.jetbrains.kotlin.android")
    id ("com.google.devtools.ksp") version "2.0.0-1.0.21"
    id ("com.google.protobuf") version "0.9.4"
    id ("de.mannodermaus.android-junit5")
}

android {
    namespace = "com.desarrollodroide.data"
    compileSdk = (findProperty("compileSdkVersion") as String).toInt()

    defaultConfig {
        testInstrumentationRunnerArguments += mapOf("runnerBuilder" to "de.mannodermaus.junit5.AndroidJUnit5Builder")
        minSdk = (findProperty("minSdkVersion") as String).toInt()
        targetSdk = (findProperty("targetSdkVersion") as String).toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    packagingOptions {
        jniLibs {
            excludes += setOf("META-INF/LICENSE*")
        }
        resources {
            excludes += setOf("META-INF/LICENSE*")
        }
    }
    // JUnit 5 will bundle in files with identical paths, exclude them
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
    // Project module dependencies
    implementation(project(":network"))
    implementation(project(":model"))
    implementation(project(":common"))

    // Retrofit for HTTP requests and networking
    implementation (libs.bundles.retrofit) // Retrofit with logging, Gson, and scalar converters for REST API communication.

    // Koin for dependency injection, specifically tailored for use with Jetpack Compose
    implementation (libs.koin.androidx.compose) // Koin library for dependency injection within Android Compose applications.

    // AndroidX core libraries for fundamental functionality
    implementation (libs.androidx.core) // Core utility functions and backward-compatible versions of Android framework components.
    implementation (libs.androidx.datastore.preferences) // DataStore for storing key-value pairs asynchronously and transactionally.
    implementation (libs.androidx.datastore.core) // Core DataStore functionality.
    implementation (libs.androidx.paging.compose) // Paging library for Jetpack Compose.
    implementation (libs.androidx.lifecycle.runtime) // Lifecycle components for Jetpack Compose.

    // Protocol Buffers for efficient serialization of structured data
    implementation(libs.protobuf.kotlin.lite) // Protocol Buffers Lite for Kotlin, for efficient data serialization.

    // Room for abstracting SQLite database access and providing compile-time checks of SQL queries
    implementation(libs.androidx.room) // Room for database access, abstracting SQLite and providing LiveData support.
    ksp(libs.androidx.room.compiler) // Kotlin Symbol Processing (KSP) for Room to generate database access code at compile time.
    implementation(libs.androidx.room.paging) // Replace with the appropriate version if different.

    // WorkManager
    implementation(libs.androidx.work) // WorkManager for managing background tasks.

    // Testing libraries
    testImplementation(libs.junit.jupiter) // JUnit Jupiter for unit testing with JUnit 5.
    testRuntimeOnly(libs.junit.jupiter.engine) // JUnit Jupiter Engine for running JUnit 5 tests.
    testImplementation(libs.junit.jupiter.api) // JUnit Jupiter API for writing tests and extensions in JUnit 5.
    testImplementation(libs.mockito.core) // Mockito for mocking objects in tests.
    testImplementation(libs.mockito.kotlin) // Kotlin extension for Mockito to better support Kotlin features.
    testImplementation(libs.kotlin.coroutines.test) // Coroutines Test library for testing Kotlin coroutines.
    testImplementation(libs.kotlin.test.junit5) // Kotlin Test library for JUnit 5 support.
    testImplementation(libs.androidx.paging.common) // Common Paging library for testing.
    testImplementation("app.cash.turbine:turbine:1.1.0") // Turbine for testing flows.


    // Android Testing libraries
    androidTestImplementation ("androidx.test:core:1.5.0") // Core testing library for Android, providing API for test infrastructure.
    androidTestImplementation ("androidx.test:runner:1.5.0") // Android Test Runner for running instrumented tests.
    androidTestImplementation ("androidx.test:rules:1.5.0") // Android Test Rules for defining complex test cases.
    androidTestImplementation(libs.androidx.room.testing) // Room Testing support for testing Room databases.
    androidTestImplementation(libs.kotlin.coroutines.test) // Coroutines Test library for testing coroutines in Android tests.
    androidTestImplementation("de.mannodermaus.junit5:android-test-core:1.2.2") // Android support for JUnit 5 tests.
    androidTestRuntimeOnly("de.mannodermaus.junit5:android-test-runner:1.2.2") // JUnit 5 Runner for running Android tests with JUnit 5.
}


// Setup protobuf configuration, generating lite Java and Kotlin classes
protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                val java by registering {
                    option("lite")
                }
                val kotlin by registering {
                    option("lite")
                }
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = true
    }
}