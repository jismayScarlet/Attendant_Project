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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}


dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)  // 新增這一行
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("org.springframework.boot:spring-boot-starter-web")

    /* chatGPT API對接依賴*/
    implementation("com.squareup.okhttp3:okhttp:4.12.0")// OkHttp
    // 如果你使用 org.json，也需要加這個：
    implementation("org.json:json:20240303") // 可用最新版 // org.json for JSON handling (Java compatible)


}