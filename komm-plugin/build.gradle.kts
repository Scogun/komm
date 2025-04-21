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
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.20")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.0.20-1.0.25")
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