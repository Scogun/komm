plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlinx.kover)
    id("publish")
}

kotlin {
    jvmToolchain(8)
    jvm {
        tasks.withType<Test> {
            useJUnitPlatform()
            reports {
                junitXml.required.set(true)
            }
        }
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":komm-plugins-core"))
                implementation(libs.exposed.core)
                implementation(libs.kotlin.poet.ksp)
            }
            kotlin.srcDir("src/main/kotlin")
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.junit.jupiter)
                implementation(libs.kotest.assertions)
                implementation(libs.mockk)
                implementation(libs.kotlin.poet.ksp)
                implementation(kotlin("reflect"))
            }
            kotlin.srcDir("src/test/kotlin")
        }
    }
}

libraryData {
    name.set("KOMM Plugins Exposed")
    description.set("Plugins to map Exposed ResultRow to DTO Object for Kotlin Object Multiplatform Mapper Plugins")
}