pluginManagement {
    resolutionStrategy {
        plugins {
            val kotlinVersion = "2.1.0"
            kotlin("multiplatform") version kotlinVersion apply false
            id("com.google.devtools.ksp") version "$kotlinVersion-1.0.29" apply false
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "komm"

include("komm-annotations")

include("komm-plugins-core")

include("komm-plugins-exposed")

include("komm-plugins-iterable")

include("komm-processor")

include("komm-simple")