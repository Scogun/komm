plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
}

kotlin {
    jvm {
        withJava()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":komm-annotations"))
            }
        }
        val jvmMain by getting
    }
}

dependencies {
    add("kspJvm", project(":komm-processor"))
}