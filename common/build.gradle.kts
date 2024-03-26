plugins {
    id("com.android.library")
    id ("org.jetbrains.kotlin.android")
    id ("kotlin-kapt")
}

android {
    namespace = "com.desarrollodroide.common"
    compileSdk = (findProperty("compileSdkVersion") as String).toInt()

    defaultConfig {
        minSdk = (findProperty("minSdkVersion") as String).toInt()
        targetSdk = (findProperty("targetSdkVersion") as String).toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
