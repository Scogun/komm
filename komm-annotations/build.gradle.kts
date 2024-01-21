plugins {
    kotlin("multiplatform")
    id("publish")
}

kotlin {
    jvm()
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