plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlinx.kover) apply false
    alias(libs.plugins.maven.publish) apply false
}

tasks.wrapper {
    gradleVersion = "9.5.1"
}

allprojects {
    group = "com.ucasoft.komm"

    version = "2.3.20-0.33.7-RC1"

    repositories {
        mavenCentral()
    }
}