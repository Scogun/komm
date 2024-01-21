plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlinx.kover)
    id("publish")
}

kotlin {
    jvm()
    js {
        nodejs()
    }
    linuxX64()
    mingwX64()
    macosX64()
    iosX64()
    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("src/main/kotlin")
        }
    }
}

libraryData {
    name.set("KOMM Annotations")
    description.set("Annotations library for Kotlin Object Multiplatform Mapper")
}

tasks {
    val publishJsPublicationToMavenLocal by getting {
        dependsOn(
            ":komm-annotations:signJvmPublication",
            ":komm-annotations:signLinuxX64Publication",
            ":komm-annotations:signMingwX64Publication"
        )
    }
    val publishJsPublicationToMavenCentralRepository by getting {
        dependsOn(
            ":komm-annotations:signJvmPublication",
            ":komm-annotations:signLinuxX64Publication",
            ":komm-annotations:signMingwX64Publication"
        )
    }
    val publishJvmPublicationToMavenLocal by getting {
        dependsOn(
            ":komm-annotations:signJsPublication",
            ":komm-annotations:signLinuxX64Publication",
            ":komm-annotations:signMingwX64Publication"
        )
    }
    val publishJvmPublicationToMavenCentralRepository by getting {
        dependsOn(
            ":komm-annotations:signJsPublication",
            ":komm-annotations:signLinuxX64Publication",
            ":komm-annotations:signMingwX64Publication"
        )
    }
    val publishLinuxX64PublicationToMavenLocal by getting {
        dependsOn(
            ":komm-annotations:signJsPublication",
            ":komm-annotations:signJvmPublication",
            ":komm-annotations:signMingwX64Publication"
        )
    }
    val publishLinuxX64PublicationToMavenCentralRepository by getting {
        dependsOn(
            ":komm-annotations:signJsPublication",
            ":komm-annotations:signJvmPublication",
            ":komm-annotations:signMingwX64Publication"
        )
    }
    val publishMingwX64PublicationToMavenLocal by getting {
        dependsOn(
            ":komm-annotations:signJsPublication",
            ":komm-annotations:signJvmPublication",
            ":komm-annotations:signLinuxX64Publication"
        )
    }
    val publishMingwX64PublicationToMavenCentralRepository by getting {
        dependsOn(
            ":komm-annotations:signJsPublication",
            ":komm-annotations:signJvmPublication",
            ":komm-annotations:signLinuxX64Publication"
        )
    }
}