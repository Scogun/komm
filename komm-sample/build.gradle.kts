plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ksp)
}

kotlin {
    jvm()
    js {
        browser()
        binaries.executable()
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":komm-annotations"))
                implementation(project(":komm-plugins-enum"))
            }
        }
        jvmMain {
            dependencies {
                implementation(libs.exposed.core)
            }
        }
        jsMain
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