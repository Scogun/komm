plugins {
    kotlin("multiplatform") version "2.1.21"
}

fun extractRootProperty(propertyName: String): String? {
    val buildFile = File(project.projectDir, "../build.gradle.kts")
    if (!buildFile.exists()) return null

    val content = buildFile.readText()
    val pattern = "$propertyName\\s*=\\s*\"([^\"]*)\"".toRegex()
    val match = pattern.find(content)

    return match?.groupValues?.getOrNull(1)
}

val parentGroup = extractRootProperty("group")
val parentVersion = extractRootProperty("version")

group = parentGroup ?: "com.ucasoft.komm"
version = parentVersion ?: "0.25.0"

repositories {
    mavenCentral()
}

kotlin {
    js {
        browser {
            webpackTask {
                mainOutputFileName.set("bundle.[chunkhash].js")
            }
        }
        binaries.executable()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(kotlinWrappers.react)
                implementation(kotlinWrappers.reactDom)
                implementation(kotlinWrappers.reactRouter)
                implementation(kotlinWrappers.mui.material)
                implementation(kotlinWrappers.emotion.react)
                implementation(kotlinWrappers.emotion.styled)
                implementation(devNpm("html-webpack-plugin", "5.6.3"))
                implementation(npm("lucide-react","0.513.0"))
                implementation(npm("react-syntax-highlighter","15.6.1"))
            }
        }
    }
}