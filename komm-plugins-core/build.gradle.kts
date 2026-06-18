plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.maven.publish)
}

kotlin {
    jvmToolchain(17)
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                api(libs.ksp.processor)
            }
            kotlin.srcDir("src/main/kotlin")
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    pom {
        configurePom("KOMM Plugins Core", "Core library for Kotlin Object Multiplatform Mapper Plugins", this)
    }
}