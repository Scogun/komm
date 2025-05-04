plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
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
                implementation(project(":komm-plugins-enum"))
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
    add("kspJvm", project(":komm-plugins-enum"))
    add("kspJvm", project(":komm-plugins-exposed"))
    add("kspJvm", project(":komm-plugins-iterable"))
    add("kspJvm", project(":komm-processor"))
    add("kspJs", project(":komm-plugins-iterable"))
    add("kspJs", project(":komm-processor"))
}