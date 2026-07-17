plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.litelauncher.runtime"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        consumerPackages("com.litelauncher.runtime")
    }

    buildFeatures {
        aidl = true
    }

    externalNativeBuild {
        cmake {
            path = file("../native/CMakeLists.txt")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
}
