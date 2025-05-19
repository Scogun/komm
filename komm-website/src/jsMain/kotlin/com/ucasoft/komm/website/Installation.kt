package com.ucasoft.komm.website

import mui.material.Box
import mui.material.Container
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.PropsWithRef
import web.cssom.Auto
import web.cssom.Color
import web.cssom.Margin
import web.cssom.Padding
import web.cssom.TextAlign
import web.cssom.px
import web.html.HTMLDivElement

val Installation = FC<PropsWithRef<HTMLDivElement>> {
    Box {
        ref = it.ref
        sx {
            padding = Padding(10.px, 0.px)
            backgroundColor = Color("background.paper")
            scrollMarginTop = 50.px
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
    kotlin("multiplatform") version "2.0.21"
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
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
                implementation("com.ucasoft.komm:komm-annotations:0.25.0") // Annotations library
            }
        }
    }
}

dependencies {
    add("kspJvm", "com.ucasoft.komm:komm-processor:0.25.0")
    add("kspJs", "com.ucasoft.komm:komm-processor:0.25.0")
    // Add other platforms like `kspAndroidNativeX64`, `kspLinuxX64`, `kspMingwX64` etc.
}""".trimIndent()
            }
        }
    }
}