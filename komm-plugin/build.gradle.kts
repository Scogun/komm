plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
    alias(libs.plugins.gradle.publish)
    alias(libs.plugins.kotlinx.kover) apply false
    id("com.google.devtools.ksp") apply false
}