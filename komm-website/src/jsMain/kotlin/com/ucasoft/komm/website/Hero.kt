package com.ucasoft.komm.website

import mui.material.Box
import mui.material.Container
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.PropsWithRef
import react.dom.html.ReactHTML.p
import web.cssom.*

val Hero = FC<PropsWithRef<*>> {
    Box {
        sx {
            background = linearGradient(135.deg, stop(Color("#667eea"), 0.pct), stop(Color("#764ba2"), 100.pct))
            padding = Padding(80.px, 0.px, 14.px)
            color = Color("white")
            textAlign = TextAlign.center
        }
        Container {
            maxWidth = "md"
            Typography {
                variant = TypographyVariant.h1
                gutterBottom = true
                sx {
                    fontSize = 4.rem
                }
                +"KOMM"
            }
            Typography {
                variant = TypographyVariant.h5
                component = p
                gutterBottom = true
                sx {
                    marginBottom = 3.px
                    fontSize = 1.5.rem
                }
                +"Kotlin Object Multiplatform Mapper"
            }
            Typography {
                variant = TypographyVariant.body1
                sx {
                    maxWidth = 700.px
                    margin = Margin(0.px, Auto.auto, 4.px)
                    fontSize = js("{ xs: '1rem', md: '1.125rem' }")
                    lineHeight = number(1.7)
                }
                +"""Effortlessly generate extension functions to map objects in your Kotlin Multiplatform projects.
        Reduce boilerplate, enhance type safety, and streamline your data transformations.""".trimIndent()
            }
        }
    }
}