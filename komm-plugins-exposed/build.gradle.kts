plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.maven.publish)
}

kotlin {
    jvmToolchain(17)
    jvm {
        tasks.withType<Test> {
            useJUnitPlatform()
            reports {
                junitXml.required.set(true)
            }
        }
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":komm-plugins-core"))
                implementation(libs.exposed.core)
                implementation(libs.kotlin.poet.ksp)
            }
            kotlin.srcDir("src/main/kotlin")
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.junit.jupiter)
                implementation(libs.kotest.assertions)
                implementation(libs.mockk)
                implementation(libs.kotlin.poet.ksp)
                implementation(kotlin("reflect"))
            }
            kotlin.srcDir("src/test/kotlin")
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    pom {
        configurePom("KOMM Plugins Exposed", "Plugins to map Exposed ResultRow to DTO Object for Kotlin Object Multiplatform Mapper Plugins", this)
    }
}