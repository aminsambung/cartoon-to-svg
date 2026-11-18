plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.edgeglow"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.edgeglow"
        minSdk = 23
        targetSdk = 35
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
    
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Wajib untuk AppCompatActivity dan Theme.AppCompat
    implementation("androidx.appcompat:appcompat:1.6.1")
    
    // Opsional, untuk komponen UI tambahan jika nanti butuh
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Kotlin Standar
    implementation("androidx.core:core-ktx:1.12.0")
}
