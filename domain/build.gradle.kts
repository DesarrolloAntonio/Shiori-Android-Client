plugins {
    id("com.android.library")
    id ("org.jetbrains.kotlin.android")
    id ("kotlin-kapt")
}

android {
    namespace = "com.desarrollodroide.domain"
    compileSdk = (findProperty("compileSdkVersion") as String).toInt()

    defaultConfig {
        minSdk = 21
        targetSdk = (findProperty("targetSdkVersion") as String).toInt()

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}


dependencies {

    implementation(project(":data"))
    implementation(project(":model"))
    implementation(project(":common"))

    // coroutines
    implementation (libs.kotlinx.coroutines.android)
    testImplementation (libs.kotlinx.coroutines.android)
    testImplementation (libs.kotlin.coroutines.test)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}
