import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.myhiking.log"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.myhiking.log"
        minSdk = 26
        targetSdk = 34

        val major = (project.findProperty("VERSION_MAJOR") as String).toInt()
        val minor = (project.findProperty("VERSION_MINOR") as String).toInt()
        val patch = (project.findProperty("VERSION_PATCH") as String).toInt()
        versionCode = major * 10000 + minor * 100 + patch
        versionName = "$major.$minor.$patch"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // API keys from local.properties
        val localProperties = Properties().apply {
            val localPropertiesFile = rootProject.file("local.properties")
            if (localPropertiesFile.exists()) load(localPropertiesFile.inputStream())
        }
        buildConfigField("String", "SUPABASE_URL", "\"${localProperties.getProperty("SUPABASE_URL", "")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProperties.getProperty("SUPABASE_ANON_KEY", "")}\"")
        buildConfigField("String", "FOREST_API_KEY", "\"${localProperties.getProperty("FOREST_API_KEY", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            // Robolectric: Compose layout tests at different window sizes
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Adaptive (window size class, posture, pane scaffolds, navigation suite)
    implementation(libs.androidx.material3.adaptive)
    implementation(libs.androidx.material3.adaptive.layout)
    implementation(libs.androidx.material3.adaptive.navigation)
    implementation(libs.androidx.material3.adaptive.navigation.suite)
    implementation(libs.androidx.window)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Coil
    implementation(libs.coil.compose)

    // Supabase
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.core)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.junit)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.androidx.window.testing)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
