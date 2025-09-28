plugins {
    kotlin("multiplatform") apply false
    id("com.google.devtools.ksp") apply false
    alias(libs.plugins.kotlinx.kover) apply false
    id("publish") apply false
}

tasks.wrapper {
    gradleVersion = "9.1.0"
}

allprojects {
    group = "com.ucasoft.komm"

    version = "0.25.0"

    repositories {
        mavenCentral()
    }
}