pluginManagement {
    resolutionStrategy {
        plugins {
            val kotlinVersion = "1.9.23"
            kotlin("multiplatform") version kotlinVersion apply false
            id("com.google.devtools.ksp") version "$kotlinVersion-1.0.19" apply false
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