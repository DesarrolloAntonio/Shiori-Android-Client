plugins {
    id("com.android.library")
    id ("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.desarrollodroide.model"
    compileSdk = (findProperty("compileSdkVersion") as String).toInt()

    defaultConfig {
        minSdk = (findProperty("minSdkVersion") as String).toInt()
        targetSdk = (findProperty("targetSdkVersion") as String).toInt()
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
