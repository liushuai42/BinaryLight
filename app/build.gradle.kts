import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val keystores = Properties().apply {
    val keystoreFile = file("keystore.properties")
    if (keystoreFile.exists()) {
        load(FileInputStream(keystoreFile))
    }
}

android {
    namespace = "org.jupnp.example.binarylight"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.jupnp.example.binarylight"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (keystores.isNotEmpty()) {
            create("release") {
                storeFile = file(keystores["storeFile"] as String)
                storePassword = keystores["storePassword"] as String
                keyAlias = keystores["keyAlias"] as String
                keyPassword = keystores["keyPassword"] as String
            }
        }
    }

    buildTypes {
        debug {
            if (keystores.isNotEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "missing_rules.txt",
            )
            if (keystores.isNotEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.bundles.navigation)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.recyclerview)

    implementation(libs.kotlin.coroutines)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.bundles.jupnp.android)
    implementation(libs.bundles.jetty)

    // Enable logback
    implementation(libs.bundles.logback)
}