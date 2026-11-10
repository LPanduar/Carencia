import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Firma del APK de producción: keystore.properties vive SÓLO en tu PC (está en .gitignore)
val firma = Properties().apply {
    val archivo = rootProject.file("keystore.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}

// URL pública de la API (MonsterASP). Se cambia en gradle.properties -> carencia.apiUrl
val apiProduccion = (project.findProperty("carencia.apiUrl") as String?) ?: "https://TU-SITIO.runasp.net/"

android {
    namespace = "com.akm.carencia"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.akm.carencia"
        minSdk = 24
        targetSdk = 37
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("produccion") {
            if (firma.isNotEmpty()) {
                storeFile = rootProject.file(firma.getProperty("storeFile"))
                storePassword = firma.getProperty("storePassword")
                keyAlias = firma.getProperty("keyAlias")
                keyPassword = firma.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // Desarrollo: API corriendo en tu PC.
            //  - Emulador: 10.0.2.2 apunta al "localhost" de tu PC.
            //  - Celular físico en tu red: cambia por la IP de tu PC (ej. http://192.168.1.50:5101/)
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:5101/\"")
        }
        release {
            // Producción: API publicada en internet (HTTPS)
            buildConfigField("String", "API_BASE_URL", "\"$apiProduccion\"")
            if (firma.isNotEmpty()) signingConfig = signingConfigs.getByName("produccion")
            // R8 desactivado: con la optimización de androidx/kotlin el APK se cerraba al abrir
            // en Android 12 (IllegalAccessError en InitializationProvider).
            optimization {
                enable = false
            }
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
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    // API REST
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    // Imágenes desde la API
    implementation(libs.coil.compose)
    // Cámara (componente del dispositivo)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    // Notificaciones en segundo plano + sesión guardada
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
