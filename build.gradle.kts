plugins {
    kotlin("multiplatform") apply false
    id("com.google.devtools.ksp") apply false
    alias(libs.plugins.kotlinx.kover) apply false
    alias(libs.plugins.maven.publish) apply false
}

tasks.wrapper {
    gradleVersion = "9.1.0"
}

allprojects {
    group = "com.ucasoft.komm"

    version = "2.2.20-0.28.7-RC1"

    repositories {
        mavenCentral()
    }
}