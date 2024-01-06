plugins {
    kotlin("multiplatform") version "1.9.22"
}

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":komm-annotations"))
                implementation("com.google.devtools.ksp:symbol-processing-api:1.9.22-1.0.16")
            }
            kotlin.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")
        }
    }
}