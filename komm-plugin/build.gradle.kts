plugins {
    kotlin("jvm")
    alias(libs.plugins.gradle.publish)
    id("java-gradle-plugin")
    id("com.google.devtools.ksp") apply false
    alias(libs.plugins.kotlinx.kover) apply false
}

tasks {
    test {
        useJUnitPlatform()
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
}

gradlePlugin {
    plugins {
        create("komm") {
            id = "com.ucasoft.komm"
            implementationClass = "com.ucasoft.komm.gradle.KOMMPlugin"
        }
    }
}