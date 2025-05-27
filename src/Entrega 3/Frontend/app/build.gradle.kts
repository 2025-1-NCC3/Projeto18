plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "br.fecap.pi.ubershield"
    compileSdk = 35

    defaultConfig {
        applicationId = "br.fecap.pi.ubershield"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Mapa e Rota
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.volley)
    implementation("com.google.maps.android:maps-utils-ktx:3.1.0")
    implementation("com.google.android.libraries.places:places:3.4.0")

    // HTTP Servidor
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Biometria
    implementation("androidx.biometric:biometric:1.1.0")

    // Animação e Imagem
    implementation("com.airbnb.android:lottie:6.1.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
}