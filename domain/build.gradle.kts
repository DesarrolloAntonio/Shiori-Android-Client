plugins {
    id("com.android.library")
    id ("org.jetbrains.kotlin.android")
    id ("kotlin-kapt")
}

android {
    namespace = "com.desarrollodroide.domain"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        targetSdk = 33

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
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")
//    implementation ("javax.inject:javax.inject:1")
}
