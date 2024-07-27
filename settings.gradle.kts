pluginManagement {
    resolutionStrategy {
        plugins {
            val kotlinVersion = "2.0.0"
            kotlin("multiplatform") version kotlinVersion apply false
            id("com.google.devtools.ksp") version "$kotlinVersion-1.0.22" apply false
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "komm"

include("komm-annotations")

include("komm-plugins-core")

include("komm-plugins-exposed")

include("komm-plugins-iterable")

include("komm-processor")

include("komm-simple")