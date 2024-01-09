plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm {
        tasks.withType<Test> {
            useJUnitPlatform()
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
            }
            kotlin.srcDir("src/test/kotlin")
        }
    }
}