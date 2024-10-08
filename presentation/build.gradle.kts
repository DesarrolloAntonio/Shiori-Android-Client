plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("de.mannodermaus.android-junit5")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.desarrollodroide.pagekeeper"
    compileSdk = (findProperty("compileSdkVersion") as String).toInt()

    defaultConfig {
        applicationId = "com.desarrollodroide.pagekeeper"
        minSdk = (findProperty("minSdkVersion") as String).toInt()
        targetSdk = (findProperty("targetSdkVersion") as String).toInt()
        versionCode = (findProperty("versionCode") as String).toInt()
        versionName = findProperty("versionName") as String

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("production") {
            keyAlias = System.getenv("PRODUCTION_KEY_ALIAS")
            keyPassword = System.getenv("PRODUCTION_KEY_PASSWORD")
            storeFile = file("${System.getenv("GITHUB_WORKSPACE")}/production_key_store.jks")
            storePassword = System.getenv("PRODUCTION_STORE_PASSWORD")
        }
        create("staging") {
            keyAlias = System.getenv("STAGING_KEY_ALIAS")
            keyPassword = System.getenv("STAGING_KEY_PASSWORD")
            storeFile = file("${System.getenv("GITHUB_WORKSPACE")}/staging_key_store.jks")
            storePassword = System.getenv("STAGING_STORE_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
        debug {
            isDebuggable = true
        }
    }

    flavorDimensions += "version"
    productFlavors {
        create("production") {
            dimension = "version"
            signingConfig = signingConfigs.getByName("production")
        }
        create("staging") {
            dimension = "version"
            applicationId = "com.desarrollodroide.pagekeeper.staging"
            signingConfig = signingConfigs.getByName("staging")
            versionNameSuffix = "-staging"
            resValue("string", "app_name", "Shiori-dev")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    applicationVariants.configureEach {
        outputs.configureEach {
            val output = this as? com.android.build.gradle.internal.api.BaseVariantOutputImpl
            output?.outputFileName = "Shiori v$versionName.apk"
        }
    }


    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {

    implementation(project(":data"))
    implementation(project(":domain"))
    implementation(project(":model"))
    implementation(project(":network"))
    implementation(project(":common"))

    implementation (libs.androidx.core)
    implementation (libs.androidx.lifecycle.runtime )
    implementation (libs.androidx.activity.compose)
    implementation (libs.androidx.navigation.compose)
    implementation (libs.androidx.lifecycle.viewmodel.compose)
    implementation (libs.androidx.lifecycle.runtimeCompose)
    implementation (libs.androidx.preference)
    implementation (libs.androidx.paging.compose)
    implementation ("androidx.paging:paging-common-ktx:3.3.2")

    implementation (libs.compose.ui.ui)
    implementation (libs.compose.ui.tooling.preview)
    implementation (libs.compose.ui.tooling)
    implementation (libs.compose.material3.material3)
    implementation (libs.compose.material.iconsext)
    implementation (libs.compose.runtime.livedata)

    implementation (libs.bundles.retrofit)
    implementation (libs.accompanist.permissions)

    implementation (libs.koin.androidx.compose)
    implementation (libs.androidx.datastore.preferences)
    implementation (libs.coil.compose)

    // Testing libraries
    testImplementation(libs.junit.jupiter) // JUnit Jupiter for unit testing with JUnit 5.
    testRuntimeOnly(libs.junit.jupiter.engine) // JUnit Jupiter Engine for running JUnit 5 tests.
    testImplementation(libs.junit.jupiter.api) // JUnit Jupiter API for writing tests and extensions in JUnit 5.
    testImplementation(libs.mockito.core) // Mockito for mocking objects in tests.
    testImplementation(libs.mockito.kotlin) // Kotlin extension for Mockito to better support Kotlin features.
    testImplementation(libs.kotlin.coroutines.test) // Coroutines Test library for testing Kotlin coroutines.
    testImplementation(libs.kotlin.test.junit5) // Kotlin Test library for JUnit 5 support.

}

composeCompiler {
    enableStrongSkippingMode = true
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
