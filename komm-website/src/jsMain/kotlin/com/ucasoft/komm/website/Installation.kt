package com.ucasoft.komm.website

import mui.material.Box
import mui.material.Container
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import web.cssom.Auto
import web.cssom.Color
import web.cssom.Margin
import web.cssom.Padding
import web.cssom.TextAlign
import web.cssom.px

val Installation = FC {
    Box {
        sx {
            padding = Padding(10.px, 0.px)
            backgroundColor = Color("background.paper")
        }
        Container {
            Box {
                sx {
                    textAlign = TextAlign.center
                    marginBottom = 6.px
                }
                Typography {
                    variant = TypographyVariant.h2
                    +"Getting Started"
                }
                Typography {
                    sx {
                        color = Color("text.secondary")
                        maxWidth = 700.px
                        margin = Margin(0.px, Auto.auto)
                    }
                    +"Add KOMM to your project in just a few simple steps."
                }
            }
            Code {
                title = "build.gradle.kts (Kotlin DSL)"
                code = """plugins {
    kotlin("multiplatform") version "1.9.0"
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
}

kotlin {
    jvm()
    js(IR) {
        browser()
        nodejs()
    }
    // Other targets...

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.github.Scogun:komm-core:1.1.0") // Core library
                implementation("com.github.Scogun:komm-plugins-iterable:1.1.0") // Optional plugin
            }
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", "com.github.Scogun:komm-processor:1.1.0")
}""".trimIndent()
            }
        }
    }
}