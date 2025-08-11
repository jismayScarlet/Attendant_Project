plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.attendant_project"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.attendant_project"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures{
        viewBinding = true
        dataBinding = true
    }
}


dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)  // 新增這一行
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
//    implementation("org.springframework.boot:spring-boot-starter-web:3.1.4") //spring boot的引入工具

    /* chatGPT API對接依賴*/
    implementation("com.squareup.okhttp3:okhttp:4.12.0")// OkHttp
    // 如果你使用 org.json，也需要加這個：
    implementation("org.json:json:20240303") // 可用最新版 // org.json for JSON handling (Java compatible)

    testImplementation(libs.junit)
    testImplementation(libs.core.testing)

    implementation ("androidx.recyclerview:recyclerview:1.2.0")
    // For control over item selection of both touch and mouse driven selection
    implementation ("androidx.recyclerview:recyclerview-selection:1.2.0")

    // Room 資料庫核心
    implementation ("androidx.room:room-runtime:2.6.1")
    annotationProcessor ("androidx.room:room-compiler:2.6.1") // Java 必需

// LiveData + ViewModel (Jetpack Lifecycle)
    implementation ("androidx.lifecycle:lifecycle-livedata:2.7.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    annotationProcessor ("androidx.lifecycle:lifecycle-compiler:2.7.0")

// 可選：使用 Room 的 Kotlin Coroutine 支援（若不使用可略）
    implementation ("androidx.room:room-ktx:2.6.1")

// 可選：使用 Room 在背景執行 Thread
    implementation ("androidx.room:room-common:2.6.1")

//    val camerax_version = "1.2.2"
    val camerax_version = "1.5.0"
    implementation ("androidx.camera:camera-core:${camerax_version}")
    implementation ("androidx.camera:camera-camera2:${camerax_version}")
    implementation ("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation ("androidx.camera:camera-video:${camerax_version}")

    implementation ("androidx.camera:camera-view:${camerax_version}")
    implementation ("androidx.camera:camera-extensions:${camerax_version}")

//圖片快取依賴
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("com.github.bumptech.glide:recyclerview-integration:4.14.2")

}