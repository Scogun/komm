plugins {
    kotlin("multiplatform")
    alias(libs.plugins.maven.publish)
}

kotlin {
    jvmToolchain(11)
    jvm()
    js {
        nodejs()
    }
    linuxX64()
    mingwX64()
    macosX64()
    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("src/main/kotlin")
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    pom {
        configurePom("KOMM Annotations", "Annotations library for Kotlin Object Multiplatform Mapper", this)
    }
}