plugins {
    kotlin("jvm")
    alias(libs.plugins.gradle.publish)
    id("java-gradle-plugin")
}

tasks {
    test {
        useJUnitPlatform()
    }
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotest.assertions)
}

gradlePlugin {
    plugins {
        create("komm") {
            id = "com.ucasoft.komm"
            implementationClass = "com.ucasoft.komm.gradle.KOMMPlugin"
        }
    }
}