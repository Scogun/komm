plugins {
    kotlin("multiplatform")
    id("publish")
    alias(libs.plugins.gradle.publish) apply false
    id("com.google.devtools.ksp") apply false
    alias(libs.plugins.kotlinx.kover) apply false
}

kotlin {
    jvm()
    js {
        nodejs()
    }
    linuxX64()
    mingwX64()
    macosX64()
    iosX64()
    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("src/main/kotlin")
        }
    }
}

libraryData {
    name.set("KOMM Annotations")
    description.set("Annotations library for Kotlin Object Multiplatform Mapper")
}