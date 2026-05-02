plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.ecosort"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.ecosort"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    //pour le tensorflow
    aaptOptions {
        noCompress += "tflite"
    }
}

dependencies {
    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    // AndroidX core
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core:1.13.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Material Design
    implementation("com.google.android.material:material:1.12.0")

    // DrawerLayout
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")

    // Fragment
    implementation("androidx.fragment:fragment:1.8.1")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // CameraX
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

    // Retrofit + Gson
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // Guava pour ListenableFuture
    implementation("com.google.guava:guava:33.2.1-android")
}