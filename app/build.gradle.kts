plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "ke.linda.donate"
    compileSdk = 35

    defaultConfig {
        applicationId = "ke.linda.donate"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        // Team-wide salt for phone-number hashes. Comes from the environment, never the repo.
        val salt = System.getenv("DONATION_SALT") ?: "dev-salt-change-me"
        buildConfigField("String", "DONATION_SALT", "\"$salt\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
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
    implementation(project(":redactor"))
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}
