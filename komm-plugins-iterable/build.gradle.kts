plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlinx.kover)
    id("publish")
    id("com.google.devtools.ksp") apply false
}

kotlin {
    jvm {
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":komm-annotations"))
                implementation(project(":komm-plugins-core"))
                implementation(libs.kotlin.poet.ksp)
            }
            kotlin.srcDir("src/main/kotlin")
        }
    }
}

libraryData {
    name.set("KOMM Plugins Iterable")
    description.set("Plugins to map Iterable properties for Kotlin Object Multiplatform Mapper Plugins")
}