package com.ucasoft.komm.website

import mui.material.Box
import mui.material.Container
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.dom.html.ReactHTML.span
import web.cssom.TextAlign
import web.cssom.px
import web.cssom.rem
import web.cssom.rgb

val Hero = FC {
    Box {
        sx {
            backgroundColor = rgb(127, 82, 255, 0.05)
            padding = 12.px
            textAlign = TextAlign.center
        }
        Container {
            Typography {
                variant = TypographyVariant.h1
                gutterBottom = true
                +"Kotlin Object "
                Typography {
                    component = span
                    variant = TypographyVariant.h1
                    asDynamic().color = "primary.main"
                    +"Multiplatform Mapper"
                }
            }
            Typography {
                variant = TypographyVariant.body1
                asDynamic().color = "secondary.main"
                sx {
                    maxWidth = 700.px
                    fontSize = 1.2.rem
                }
                +"Seamlessly map objects between different data models across multiple platforms with KOMM - a powerful KSP generator for Kotlin Multiplatform."
            }
        }
    }
}