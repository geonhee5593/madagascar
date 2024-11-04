import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.madagascar"
    compileSdk = 34

    // Properties 파일 로드
    val properties = Properties().apply {
        load(FileInputStream(rootProject.file("local.properties")))
    }

    defaultConfig {
        applicationId = "com.example.madagascar"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // NAVER MAP CLIENT ID를 AndroidManifest.xml에 전달
        addManifestPlaceholders(mapOf("NAVERMAP_CLIENT_ID" to properties.getProperty("NAVERMAP_CLIENT_ID")))

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("com.google.android.gms:play-services-location:19.0.1") // 위치 서비스를 위한 의존성 추가
    // ViewPager2 라이브러리
    implementation ("androidx.viewpager2:viewpager2:1.0.0")

    // Material Components 라이브러리 (TabLayout 포함)
    implementation ("com.google.android.material:material:1.4.0")
    // 네이버 SDK 추가
    implementation("com.naver.maps:map-sdk:3.19.1")
}