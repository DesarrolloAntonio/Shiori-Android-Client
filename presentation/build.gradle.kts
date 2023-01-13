plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.shiori.androidclient"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.shiori.androidclient"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.1.1"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation (libs.androidx.core)
    implementation (libs.androidx.lifecycle.runtime )
    implementation (libs.androidx.activity.compose)
    implementation (libs.androidx.navigation.compose)
    implementation (libs.androidx.preference)

    implementation (libs.compose.ui.ui)
    implementation (libs.compose.ui.tooling.preview)
    debugImplementation (libs.compose.ui.tooling)
    implementation (libs.compose.material3.material3)
    implementation (libs.compose.runtime.livedata)

    implementation (libs.bundles.koin)
    implementation (libs.bundles.retrofit)
    implementation (libs.accompanist.permissions)

}