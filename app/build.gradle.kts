import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services") version "4.4.2"
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

val cloudName = localProperties.getProperty("CLOUDINARY_CLOUD_NAME") ?: "mc7vbbnp"
val uploadPreset = localProperties.getProperty("CLOUDINARY_UPLOAD_PRESET") ?: "vow3adsr"
val geminiKey = localProperties.getProperty("GEMINI_API_KEY") ?: ""

android {
    namespace = "com.globalcampus.support"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.globalcampus.support"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"$cloudName\"")
        buildConfigField("String", "CLOUDINARY_UPLOAD_PRESET", "\"$uploadPreset\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")
        buildConfigField("String", "AGORA_APP_ID", "\"${localProperties.getProperty("AGORA_APP_ID") ?: ""}\"")
        buildConfigField("String", "AGORA_TEMP_TOKEN", "\"${localProperties.getProperty("AGORA_TEMP_TOKEN") ?: ""}\"")
        buildConfigField("String", "AGORA_CHANNEL_NAME", "\"${localProperties.getProperty("AGORA_CHANNEL_NAME") ?: "Test"}\"")
    }
    buildFeatures { buildConfig = true; compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation(platform("androidx.compose:compose-bom:2024.09.03"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.navigation:navigation-compose:2.8.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("io.agora.rtc:full-sdk:4.4.0")
}
