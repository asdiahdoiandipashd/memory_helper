// 文件位置: E:\memory_helper\app\build.gradle.kts

plugins {
    // ⚠️ 注意：这里是应用插件，千万【不要】加 apply false
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // 应用功能插件
    alias(libs.plugins.ksp)  // 用于 Room 和 Hilt 的代码生成
    alias(libs.plugins.hilt) // 依赖注入
    alias(libs.plugins.kotlin.serialization) // JSON 解析
}

android {
    // 1. 命名空间 (必须有，修复 'Namespace not specified' 报错)
    namespace = "com.example.memoryhelper"

    // 2. 编译 SDK 版本 (建议用 34 或 35)
    compileSdk = 35

    defaultConfig {
        // 应用 ID (手机上显示的包名)
        applicationId = "com.example.memoryhelper"
        minSdk = 26 // Android 8.0+
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true // 开启 Compose
    }
}

// 👇👇👇 所有的第三方库依赖都放在这里！ 👇👇👇
dependencies {
    // --- 基础安卓库 ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.documentfile)

    // --- 1. Room 数据库 (本地存储) ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler) // 代码生成器

    // --- 2. Hilt (依赖注入) ---
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler) // 代码生成器

    // --- 3. Navigation Compose (导航) ---
    implementation(libs.navigation.compose)

    // --- 3. Vico (图表库) ---
    implementation(libs.vico.compose)

    // --- 4. Coil (图片加载) ---
    implementation(libs.coil.compose)

    // --- 5. Glance (桌面小组件) ---
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // --- 6. Serialization (JSON 数据备份) ---
    implementation(libs.kotlinx.serialization.json)

    // --- 测试相关 ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}