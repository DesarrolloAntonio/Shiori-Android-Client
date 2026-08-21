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
            keyAlias = System.getenv("RELEASE_KEY_ALIAS")
            keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
            storeFile = file("${System.getenv("GITHUB_WORKSPACE")}/key_store.jks")
            storePassword = System.getenv("RELEASE_STORE_PASSWORD")
        }
        create("staging") {
            keyAlias = System.getenv("RELEASE_KEY_ALIAS")
            keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
            storeFile = file("${System.getenv("GITHUB_WORKSPACE")}/key_store.jks")
            storePassword = System.getenv("RELEASE_STORE_PASSWORD")
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
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
    implementation (libs.androidx.paging.common)

    // Compose: the BOM pins every androidx.compose.* artifact, including Material 3 Expressive.
    implementation (platform(libs.compose.bom))
    androidTestImplementation (platform(libs.compose.bom))
    androidTestImplementation (libs.compose.ui.test.junit4)
    androidTestImplementation (libs.androidx.test.ext.junit)
    // ui-test-manifest supplies the empty ComponentActivity that createComposeRule() launches;
    // without it the tests fail with ActivityNotFoundException.
    debugImplementation (libs.compose.ui.test.manifest)
    implementation (libs.bundles.compose)

    implementation (libs.bundles.retrofit)

    implementation (libs.koin.androidx.compose)
    implementation (libs.androidx.datastore.preferences)
    implementation (libs.coil.compose)
    implementation (libs.coil.network.okhttp)

    // Testing libraries
    testImplementation(libs.junit.jupiter) // JUnit Jupiter for unit testing with JUnit 5.
    testRuntimeOnly(libs.junit.jupiter.engine) // JUnit Jupiter Engine for running JUnit 5 tests.
    testImplementation(libs.junit.jupiter.api) // JUnit Jupiter API for writing tests and extensions in JUnit 5.
    testImplementation(libs.mockito.core) // Mockito for mocking objects in tests.
    testImplementation(libs.mockito.kotlin) // Kotlin extension for Mockito to better support Kotlin features.
    testImplementation(libs.kotlin.coroutines.test) // Coroutines Test library for testing Kotlin coroutines.
    testImplementation(libs.kotlin.test.junit5) // Kotlin Test library for JUnit 5 support.

}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
