plugins {
    id ("com.android.library")
    id ("org.jetbrains.kotlin.android")
}


android {
    namespace = "com.desarrollodroide.network"
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {

    implementation(project(":common"))
    implementation (libs.bundles.retrofit)
    implementation (libs.koin.androidx.compose)

}
