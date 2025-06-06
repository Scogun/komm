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
    js {
        nodejs()
    }
    linuxX64()
    mingwX64()
    macosX64()
    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":komm-annotations"))
                implementation(project(":komm-plugins-core"))
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
    name.set("KOMM Plugins Enum")
    description.set("Plugins to map enum properties for Kotlin Object Multiplatform Mapper Plugins")
}