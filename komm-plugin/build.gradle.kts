plugins {
    kotlin("jvm")
    alias(libs.plugins.gradle.publish)
    id("java-gradle-plugin")
    id("com.google.devtools.ksp") apply false
    alias(libs.plugins.kotlinx.kover) apply false
}