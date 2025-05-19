package com.ucasoft.komm.website

import com.ucasoft.wrappers.lucide.*
import mui.material.Box
import mui.material.Container
import mui.material.Grid
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.FC
import react.PropsWithRef
import react.create
import web.cssom.*
import web.html.HTMLDivElement

val features = listOf(
    IconItem(Cpu.create { size = 36 }, "KSP Multiplatform Support", description = "Utilizes Kotlin Symbol Processing (KSP) for efficient, multiplatform code generation."),
    IconItem(Settings.create { size = 36 }, "Constructor & Property Mapping", "Maps to constructor parameters and public properties with setters for maximum flexibility."),
    IconItem(Zap.create { size = 36 }, "Property Type Casting", "Supports automatic casting of property types during mapping operations."),
    IconItem(FileJson.create { size = 36 }, "Java `get*` Function Support", "Seamlessly maps from Java objects using their `get*` accessor methods."),
    IconItem(Layers.create { size = 36 }, "Multi-Source Class Mapping", "Map data from multiple source classes with distinct configurations for each."),
    IconItem(Tag.create { size = 36 }, "Advanced Annotations", "Fine-tune mappings with annotations for different names, converters, resolvers, and null substitutes.")
)

val Features = FC<PropsWithRef<HTMLDivElement>> {
    Box {
        ref = it.ref
        sx {
            padding = Padding(12.px, 0.px)
            scrollMarginTop = 50.px
        }
        Container {
            maxWidth = "lg"
            Typography {
                variant = TypographyVariant.h3
                sx {
                    fontWeight = FontWeight.bold
                    textAlign = TextAlign.center
                    marginBottom = 8.px
                    color = Color("text.primary")
                }
                +"Powerful Features"
            }
            Grid {
                container = true
                spacing = responsive(3)
                features.map {
                    Grid {
                        item = true
                        asDynamic().xs = responsive(12)
                        asDynamic().sm = responsive(6)
                        asDynamic().md = responsive(4)
                        Feature {
                            icon = it.icon
                            title = it.title
                            description = it.description
                        }
                    }
                }
            }
        }
    }
}