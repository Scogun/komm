pluginManagement {
    resolutionStrategy {
        plugins {
            kotlin("multiplatform") version "2.3.20" apply false
            id("com.google.devtools.ksp") version "2.3.6" apply false
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "komm"

include("komm-annotations")

include("komm-plugins-core")

include("komm-plugins-enum")

include("komm-plugins-exposed")

include("komm-plugins-iterable")

include("komm-processor")

include("komm-simple")