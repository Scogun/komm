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

    version = "0.70.6"

    repositories {
        mavenCentral()
    }
}