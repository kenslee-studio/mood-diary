
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("com.google.devtools.ksp")
    id ("dagger.hilt.android.plugin")
    id ("io.realm.kotlin")
    id("com.google.gms.google-services")
}

android {
    namespace = "in.kenslee.MultiModuleDiary"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "in.kenslee.MultiModuleDiary"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

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
        kotlinCompilerExtensionVersion = "1.4.6"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:utils"))
    implementation(project(":data:mongo"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:home"))
    implementation(project(":feature:write"))

    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth)

    implementation (libs.core.ktx)
    implementation (libs.lifecycle.runtime)
    implementation (libs.activity.compose)
    implementation (libs.compose.ui)
    implementation (libs.compose.tooling.preview)
    implementation (libs.material3.compose)
    debugImplementation (libs.compose.ui.tooling)
    debugImplementation (libs.compose.ui.test.manifest)

    // Compose Navigation
    implementation (libs.navigation.compose)


    // Room components
    implementation (libs.room.runtime)
    ksp (libs.room.compiler)
    implementation( libs.room.ktx)

    // Runtime Compose
    implementation (libs.runtime.compose)

    // Splash API
    implementation( libs.splash.api)

    // Mongo DB Realm
    implementation (libs.coroutines.core)
    implementation( libs.realm.sync)

    // Dagger Hilt
    implementation (libs.hilt.android)
    ksp (libs.hilt.compiler)
    implementation (libs.hilt.navigation.compose)
}