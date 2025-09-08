plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.pgzxc.updateforce"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pgzxc.updateforce"
        minSdk = 25
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        //buildConfigField("int", "VERSION_CODE", versionCode.toString())
        buildConfigField("int", "VERSION_CODE", "$versionCode")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


    }

    // 启用 BuildConfig 生成功能
    buildFeatures {
        buildConfig = true
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

   // OkHttp3
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
}