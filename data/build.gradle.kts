import com.google.protobuf.gradle.builtins
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    id ("com.android.library")
    id ("org.jetbrains.kotlin.android")
    id ("kotlin-kapt")
    id("com.google.protobuf") version "0.8.19"
}

android {
    namespace = "com.shiori.data"
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

    implementation(project(":network"))
    implementation(project(":model"))
    implementation(project(":common"))

    implementation (libs.bundles.retrofit)


//    // Koin
//    def koin_version= "3.2.0"
//    implementation "io.insert-koin:koin-androidx-compose:$koin_version"
//    //implementation "io.insert-koin:koin-androidx-navigation:$koin_version"
//    implementation "io.insert-koin:koin-android:$koin_version"

    implementation ("io.insert-koin:koin-androidx-compose:3.4.1")
//    implementation ("io.insert-koin:koin-android:3.3.2")
//    implementation ("io.insert-koin:koin-core:3.3.2")

    //implementation (libs.bundles.koin)


    implementation ("androidx.core:core-ktx:1.9.0")
    implementation ("androidx.datastore:datastore-preferences:1.0.0")
    implementation ("androidx.datastore:datastore-core:1.0.0")
//    implementation ("com.google.protobuf:protobuf-javalite:3.18.0")
    implementation(libs.protobuf.kotlin.lite)
    implementation(libs.androidx.room)
    kapt(libs.androidx.room.compiler)

}

// Setup protobuf configuration, generating lite Java and Kotlin classes
protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
//    protoc {
//        artifact = libs.protobuf.kotlin.lite.get().toString()
//    }

//    plugins {
//        id("grpc"){
//            artifact = "io.grpc:protoc-gen-grpc-java:1.33.1"
//        }
//        id("grpckt") {
//            artifact = "io.grpc:protoc-gen-grpc-kotlin:0.1.5"
//        }
//    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                //remove("java")
//                remove java
                val java by registering {
                    option("lite")
                }
                val kotlin by registering {
                    option("lite")
                }
            }
//            task.plugins {
//
//            }
        }
    }
}


