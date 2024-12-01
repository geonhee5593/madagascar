import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.madagascar"
    compileSdk = 35

    // Properties 파일 로드
    val properties = Properties().apply {
        load(FileInputStream(rootProject.file("local.properties")))
    }

    defaultConfig {
        applicationId = "com.example.madagascar"
        minSdk = 30
        targetSdk = 35
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

    implementation ("androidx.recyclerview:recyclerview:1.3.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.code.gson:gson:2.8.9")
    implementation ("com.github.bumptech.glide:glide:4.13.0")
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.generativeai)
    implementation(libs.androidx.foundation.android)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.13.0")

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

    //api 병렬 처리
    // Retrofit RxJava3 어댑터
    implementation ("com.squareup.retrofit2:adapter-rxjava3:2.9.0")

    // RxJava3
    implementation ("io.reactivex.rxjava3:rxjava:3.1.6")

    // RxAndroid3
    implementation ("io.reactivex.rxjava3:rxandroid:3.0.2")


    // 위치 서비스를 위한 의존성 추가
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("com.google.android.gms:play-services-location:19.0.1")

    // ViewPager2 라이브러리
    implementation ("androidx.viewpager2:viewpager2:1.0.0")

    // Material Components 라이브러리 (TabLayout 포함)
    implementation ("com.google.android.material:material:1.4.0")
    // 네이버 SDK 추가
    implementation("com.naver.maps:map-sdk:3.19.1")

    // 파이어베이스
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-firestore-ktx:24.6.1")
    //구글플레이


}
