plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
    alias(libs.plugins.kotlinx.kover) apply false
}

kotlin {
    jvm {
        withJava()
    }
    js {
        browser()
        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":komm-annotations"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.exposed.core)
            }
        }
        val jsMain by getting
    }
}

dependencies {
    add("kspJvm", project(":komm-plugins-exposed"))
    add("kspJvm", project(":komm-plugins-iterable"))
    add("kspJvm", project(":komm-processor"))
    add("kspJs", project(":komm-plugins-iterable"))
    add("kspJs", project(":komm-processor"))
}