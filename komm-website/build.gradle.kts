plugins {
    kotlin("multiplatform") version "2.4.0"
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
version = parentVersion ?: "0.50.9"

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
                implementation(kotlinWrappers.js)
                implementation(kotlinWrappers.tanstack.reactRouter)
                implementation(kotlinWrappers.mui.material)
                implementation(kotlinWrappers.emotion.react)
                implementation(kotlinWrappers.emotion.styled)
                implementation(devNpm("html-webpack-plugin", "5.6.7"))
                implementation(npm("lucide-react","1.21.0"))
                implementation(npm("react-syntax-highlighter","16.1.1"))
            }
        }
    }
}