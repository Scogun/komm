package com.ucasoft.komm.website

import mui.material.Box
import mui.material.Container
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.dom.html.ReactHTML.span
import web.cssom.Auto
import web.cssom.Margin
import web.cssom.Padding
import web.cssom.TextAlign
import web.cssom.px
import web.cssom.rem
import web.cssom.rgb

val Hero = FC {
    Box {
        sx {
            backgroundColor = rgb(127, 82, 255, 0.05)
            padding = Padding(12.px, 0.px)
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
                asDynamic().color = "text.secondary"
                sx {
                    maxWidth = 700.px
                    margin = Margin(0.px, Auto.auto, 4.px)
                    fontSize = js("{ xs: '1rem', md: '1.2rem' }")
                }
                +"Seamlessly map objects between different data models across multiple platforms with KOMM - a powerful KSP generator for Kotlin Multiplatform."
            }
        }
    }
}