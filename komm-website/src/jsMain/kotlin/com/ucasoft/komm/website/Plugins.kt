package com.ucasoft.komm.website

import com.ucasoft.wrappers.lucide.AlignJustify
import com.ucasoft.wrappers.lucide.Database
import com.ucasoft.wrappers.lucide.ListTree
import com.ucasoft.wrappers.lucide.Puzzle
import com.ucasoft.wrappers.lucide.SquareTerminal
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
import react.dom.aria.AriaRole.Companion.menubar
import web.cssom.Auto
import web.cssom.Color
import web.cssom.FontWeight
import web.cssom.JustifyContent
import web.cssom.Margin
import web.cssom.Padding
import web.cssom.PropertyName.Companion.justifyContent
import web.cssom.TextAlign
import web.cssom.px
import web.html.HTMLDivElement

val plugins = listOf(
    IconItem(ListTree.create { size = 40 }, "Iterable Plugin", "Supports mapping collections with different types of elements, simplifying list transformations."),
    IconItem(Database.create { size = 40 }, "Exposed Plugin", "Provides mapping from Exposed Table Objects (ResultRow) for easy database interaction."),
    IconItem(Puzzle.create { size = 40 }, "Enum Plugin", "Supports mapping enums from other enums, including default value annotations for robustness.")
)

val Plugins = FC<PropsWithRef<HTMLDivElement>> {
    Box {
        ref = it.ref
        sx {
            padding = Padding(12.px, 0.px)
            backgroundColor = Color("grey.100")
            scrollMarginTop = 50.px
        }
        Container {
            Typography {
                variant = TypographyVariant.h3
                sx {
                    fontWeight = FontWeight.bold
                    textAlign = TextAlign.center
                    marginBottom = 8.px
                    color = Color("text.primary")
                }
                +"Extend with Plugins"
            }
            Typography {
                variant = TypographyVariant.h6
                sx {
                    textAlign = TextAlign.center
                    color = Color("text.secondary")
                    margin = Margin(0.px, Auto.auto, 8.px)
                    maxWidth = 700.px
                }
                +"KOMM is designed to be extensible. Default plugins provide out-of-the-box support for common scenarios."
            }
            Grid {
                container = true
                spacing = responsive(4)
                sx {
                    justifyContent = JustifyContent.center
                }
                plugins.map {
                    Grid {
                        item = true
                        asDynamic().xs = 12
                        asDynamic().sm = 6
                        asDynamic().md = 4
                        Plugin {
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