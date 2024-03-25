plugins {
    id ("com.android.library")
    id ("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "1.9.23-1.0.19"
    id ("com.google.protobuf") version "0.9.4"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {

    implementation(project(":network"))
    implementation(project(":model"))
    implementation(project(":common"))

    implementation (libs.bundles.retrofit)
    implementation (libs.koin.androidx.compose)
    implementation (libs.androidx.core)
    implementation (libs.androidx.datastore.preferences)
    implementation (libs.androidx.datastore.core)
    implementation(libs.protobuf.kotlin.lite)
    implementation(libs.androidx.room)
    ksp(libs.androidx.room.compiler)

    // Test
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.junit.platform.suite.api)

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
}