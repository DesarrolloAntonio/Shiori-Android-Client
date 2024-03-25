plugins {
    id("kotlin")
}

dependencies {
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