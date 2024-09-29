plugins {
    kotlin("multiplatform")
    id("publish")
}

kotlin {
    jvmToolchain(8)
    jvm {
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