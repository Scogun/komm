dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("kotlinWrappers") {
            val wrappersVersion = "2025.6.3"
            from("org.jetbrains.kotlin-wrappers:kotlin-wrappers-catalog:$wrappersVersion")
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "komm-website"