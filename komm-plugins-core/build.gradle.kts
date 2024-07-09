plugins {
    kotlin("multiplatform")
    id("publish")
    alias(libs.plugins.kotlinx.kover) apply false
    id("com.google.devtools.ksp") apply false
}

kotlin {
    jvm {
        jvmToolchain(8)
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                api(libs.ksp.processor)
            }
            kotlin.srcDir("src/main/kotlin")
        }
    }
}

libraryData {
    name.set("KOMM Plugins Core")
    description.set("Core library for Kotlin Object Multiplatform Mapper Plugins")
}