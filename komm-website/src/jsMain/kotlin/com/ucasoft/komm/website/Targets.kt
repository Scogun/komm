package com.ucasoft.komm.website

import mui.material.Box
import mui.material.Grid
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.Container
import mui.system.responsive
import mui.system.sx
import react.FC
import web.cssom.Auto
import web.cssom.Color
import web.cssom.Margin
import web.cssom.Padding
import web.cssom.TextAlign
import web.cssom.px

val targetList = mapOf(
    "JVM" to "Full support for Java Virtual Machine platforms.",
    "JavaScript" to "Seamless integration with JavaScript environments.",
    "Linux" to "Native support for Linux platforms.",
    "Windows" to "Native support for Windows (mingwX64).",
    "macOS" to "Native support for macOS platforms.",
    "iOS" to "Native support for iOS development."
)

val Targets = FC {
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
                    +"Multiplatform Support"
                }
                Typography {
                    sx {
                        color = Color("text.secondary")
                        maxWidth = 700.px
                        margin = Margin(0.px, Auto.auto)
                    }
                    +"KOMM works seamlessly across all major Kotlin Multiplatform targets."
                }
            }
            Grid {
                container = true
                spacing = responsive(3)
                targetList.map {
                    Grid {
                        item = true
                        asDynamic().xs = responsive(12)
                        asDynamic().sm = responsive(6)
                        asDynamic().md = responsive(4)
                        Target {
                            title = it.key
                            description = it.value
                        }
                    }
                }
            }
        }
    }
}