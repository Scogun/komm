plugins {
    kotlin("multiplatform")
    id("publish")
}

kotlin {
    jvm {
        jvmToolchain(8)
    }
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