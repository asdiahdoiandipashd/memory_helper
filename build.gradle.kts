// 文件位置: E:\memory_helper\build.gradle.kts

plugins {
    // ⚠️ 注意：这里所有的插件后面都必须加上 "apply false"
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // 我们新增的插件
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

// 根目录文件到此结束，通常不需要其他内容