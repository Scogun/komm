plugins {
    kotlin("multiplatform")
    id("publish")
    alias(libs.plugins.gradle.publish) apply false
    id("com.google.devtools.ksp") apply false
    alias(libs.plugins.kotlinx.kover)
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
                implementation(libs.ksp.processor)
                implementation(libs.kotlin.poet.ksp)
            }
            kotlin.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlin.compile.testing.ksp)
                implementation(libs.junit.jupiter)
                implementation(libs.kotest.assertions)
                implementation(kotlin("reflect"))
            }
            kotlin.srcDir("src/test/kotlin")
        }
    }
}

libraryData {
    name.set("KOMM Processor")
    description.set("Kotlin Object Multiplatform Mapper Processor")
}