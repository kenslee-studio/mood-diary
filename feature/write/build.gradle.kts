@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    id ("com.google.devtools.ksp")
    id ("dagger.hilt.android.plugin")
    id ("io.realm.kotlin")
}

android {
    namespace = "in.kenslee.write"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {
    implementation(libs.activity.compose)
    implementation(libs.material3.compose)
    implementation(libs.navigation.compose)
    implementation(libs.coroutines.core)
    implementation(libs.coil)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.android)
    implementation(libs.realm.sync)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.compose.tooling.preview)
    implementation(libs.date.time.picker)
    implementation(libs.date.dialog)
    implementation(libs.time.dialog)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)

    implementation(project(":core:ui"))
    implementation(project(":core:utils"))
    implementation(project(":data:mongo"))
}