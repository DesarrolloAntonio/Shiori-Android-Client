plugins {
    id ("com.android.library")
    id ("com.google.devtools.ksp")
    id ("com.google.protobuf") version "0.10.0"
}

android {
    namespace = "com.desarrollodroide.data"
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
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    testOptions {
        unitTests {
            // NetworkNoCacheResource logs with android.util.Log on its error path, which is a
            // stub that throws in JVM tests. Returning defaults lets those branches be tested.
            isReturnDefaultValues = true
        }
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
        languageVersion = JavaLanguageVersion.of(21)
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
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher) // JUnit Jupiter Engine for running JUnit 5 tests.
    testImplementation(libs.junit.jupiter.api) // JUnit Jupiter API for writing tests and extensions in JUnit 5.
    testImplementation(libs.mockito.core) // Mockito for mocking objects in tests.
    testImplementation(libs.mockito.kotlin) // Kotlin extension for Mockito to better support Kotlin features.
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.okhttp3.mockwebserver) // Serves canned Shiori responses to the real Retrofit stack.
    testImplementation(libs.kotlin.test.junit5) // Kotlin Test library for JUnit 5 support.
    testImplementation(libs.androidx.paging.common) // Common Paging library for testing.
    testImplementation("app.cash.turbine:turbine:1.1.0") // Turbine for testing flows.


    // Android Testing libraries
    androidTestImplementation ("androidx.test:core:1.5.0") // Core testing library for Android, providing API for test infrastructure.
    androidTestImplementation ("androidx.test:runner:1.5.0") // Android Test Runner for running instrumented tests.
    androidTestImplementation ("androidx.test:rules:1.5.0") // Android Test Rules for defining complex test cases.
    androidTestImplementation(libs.androidx.room.testing) // Room Testing support for testing Room databases.
    androidTestImplementation(libs.kotlin.coroutines.test) // Coroutines Test library for testing coroutines in Android tests.
}


// Setup protobuf configuration, generating lite Java and Kotlin classes
protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                // register(name) rather than "by registering": the delegate form is deprecated in
                // Gradle 9 and goes away in 10.
                register("java") {
                    option("lite")
                }
                register("kotlin") {
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

// The android-junit5 plugin drove this before. It configured the test tasks through
// unitTestVariants, which AGP 9 removed, so it stopped discovering anything at all while still
// applying cleanly. Every unit test here is JUnit 5 and every instrumented test is JUnit 4, so
// plain Gradle covers it and the plugin is gone.
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
