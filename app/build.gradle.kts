
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.hilt)
}

val envProperties = Properties().apply {
    val envFile = rootProject.file(".env")
    if (envFile.exists()) {
        envFile.inputStream().use { load(it) }
    }
}

val frontendApiBaseUrl = (envProperties.getProperty("FRONTEND_API_BASE_URL") ?: "http://10.0.2.2:8000/api/")
    .trim()
    .let { if (it.endsWith("/")) it else "$it/" }

val frontendApiBaseUrlRelease = (envProperties.getProperty("FRONTEND_API_BASE_URL_RELEASE") ?: frontendApiBaseUrl)
    .trim()
    .let { if (it.endsWith("/")) it else "$it/" }

val googleWebClientId = (envProperties.getProperty("GOOGLE_WEB_CLIENT_ID") ?: "").trim()

val frontendApiBaseUrlCandidates = (envProperties.getProperty("FRONTEND_API_BASE_URL_CANDIDATES")
    ?: listOf(
        frontendApiBaseUrl,
        frontendApiBaseUrlRelease,
        "http://10.0.2.2:8000/api/",
        "http://127.0.0.1:8000/api/"
    ).joinToString(","))
    .split(",")
    .map { it.trim() }
    .filter { it.isNotBlank() }
    .map { if (it.endsWith("/")) it else "$it/" }
    .distinct()
    .joinToString(",")

android {
    namespace = "com.example.waterquality"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.waterquality"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            buildConfigField("String", "API_BASE_URL", "\"$frontendApiBaseUrl\"")
            buildConfigField("String", "API_BASE_URL_CANDIDATES", "\"$frontendApiBaseUrlCandidates\"")
            buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("String", "API_BASE_URL", "\"$frontendApiBaseUrlRelease\"")
            buildConfigField("String", "API_BASE_URL_CANDIDATES", "\"$frontendApiBaseUrlCandidates\"")
            buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.compose.adaptive)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.coil.compose)
    implementation(libs.converter.moshi)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.logging.interceptor)
    implementation(libs.material)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp)
    implementation(libs.googleid)
    implementation(libs.play.services.location)
    implementation(libs.osmdroid)
    implementation(libs.retrofit)
    implementation(libs.navigation.compose)
    testImplementation(libs.androidx.core)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    "ksp"(libs.androidx.room.compiler)
    "ksp"(libs.moshi.kotlin.codegen)
    "ksp"(libs.hilt.compiler)
}
