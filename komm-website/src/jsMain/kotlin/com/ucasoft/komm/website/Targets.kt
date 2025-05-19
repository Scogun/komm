package com.ucasoft.komm.website

import com.ucasoft.wrappers.lucide.*
import mui.material.Box
import mui.material.Grid
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.Container
import mui.system.responsive
import mui.system.sx
import react.FC
import react.PropsWithRef
import react.create
import react.dom.html.ReactHTML.h2
import web.cssom.*
import web.html.HTMLDivElement

val targetList = listOf(
    IconItem(Cpu.create { size = 40 },"JVM", "Full support for Java Virtual Machine platforms."),
    IconItem(FileJson.create { size = 40 },"JavaScript", "Seamless integration with JavaScript environments."),
    IconItem(Terminal.create { size = 40 }, "Linux", "Native support for Linux platforms."),
    IconItem(Monitor.create { size = 40 },"Windows", "Native support for Windows (mingwX64)."),
    IconItem(Apple.create { size = 40 },"macOS", "Native support for macOS platforms."),
    IconItem(Smartphone.create { size = 40 },"iOS", "Native support for iOS development.")
)

val Targets = FC<PropsWithRef<HTMLDivElement>> {
    Box {
        ref = it.ref
        sx {
            padding = Padding(12.px, 0.px)
            backgroundColor = rgb(71, 88, 163)
            color = Color("white")
            scrollMarginTop = 50.px
        }
        Container {
            Typography {
                variant = TypographyVariant.h3
                component = h2
                sx {
                    textAlign = TextAlign.center
                    fontWeight = FontWeight.bold
                    marginBottom = 8.px
                }
                +"Supported Targets"
            }
            Grid {
                container = true
                spacing = responsive(3)
                sx {
                    justifyContent = JustifyContent.center
                }
                targetList.map {
                    Grid {
                        item = true
                        asDynamic().xs = responsive(6)
                        asDynamic().sm = responsive(4)
                        asDynamic().md = responsive(2)
                        sx {
                            textAlign = TextAlign.center
                        }
                        Target {
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