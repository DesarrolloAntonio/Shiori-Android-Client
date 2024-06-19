buildscript {
    extra["compose_ui_version"] = "1.1.1"
    dependencies {
        classpath("com.google.protobuf:protobuf-java:3.19.4")
        classpath("de.mannodermaus.gradle.plugins:android-junit5:1.6.2.0")
    }
}

plugins {
    kotlin("multiplatform").apply(false)
    kotlin("plugin.compose") apply false
    kotlin("android") apply false
    id("com.android.application") version "8.3.1" apply false
    id("com.android.library") version "8.3.1" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
