plugins {
    id("com.android.library")
    id ("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.desarrollodroide.domain"
    compileSdk = (findProperty("compileSdkVersion") as String).toInt()

    defaultConfig {
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
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
