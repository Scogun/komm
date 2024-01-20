pluginManagement {
    resolutionStrategy {
        plugins {
            val kotlinVersion = "1.9.22"
            kotlin("multiplatform") version kotlinVersion apply false
            id("com.google.devtools.ksp") version "$kotlinVersion-1.0.17" apply false
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "komm"

include("komm-annotations")

include("komm-processor")

include("komm-simple")