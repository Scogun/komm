plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlinx.kover)
    id("publish")
    id("com.google.devtools.ksp") apply false
}

kotlin {
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
                implementation("io.mockk:mockk:1.13.10")
                implementation(libs.kotlin.poet.ksp)
                implementation(kotlin("reflect"))
            }
            kotlin.srcDir("src/test/kotlin")
        }
    }
}

libraryData {
    name.set("KOMM Plugins Iterable")
    description.set("Plugins to map Iterable properties for Kotlin Object Multiplatform Mapper Plugins")
}