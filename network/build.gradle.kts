plugins {
    id ("com.android.library")
    id ("org.jetbrains.kotlin.android")
}


android {
    namespace = "com.desarrollodroide.network"
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
//
//android {
//    buildFeatures {
//        buildConfig = true
//    }
//    namespace = "com.shiori.network"
//    testOptions {
//        unitTests {
//            isIncludeAndroidResources = true
//        }
//    }
//}

dependencies {
    //implementation(project(":domain"))
    implementation(project(":common"))
    implementation (libs.bundles.retrofit)
    implementation ("io.insert-koin:koin-androidx-compose:3.4.1")

}
