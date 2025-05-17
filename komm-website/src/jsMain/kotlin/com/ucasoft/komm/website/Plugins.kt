package com.ucasoft.komm.website

import com.ucasoft.wrappers.lucide.AlignJustify
import com.ucasoft.wrappers.lucide.Database
import com.ucasoft.wrappers.lucide.SquareTerminal
import mui.material.Box
import mui.material.Typography
import mui.system.Container
import mui.system.sx
import react.FC
import react.create
import web.cssom.Auto
import web.cssom.Color
import web.cssom.Margin
import web.cssom.Padding
import web.cssom.TextAlign
import web.cssom.px

val Plugins = FC {
    Box {
        sx {
            padding = Padding(10.px, 0.px)
        }
        Container {
            Box {
                sx {
                    textAlign = TextAlign.center
                    marginBottom = 6.px
                }
                Typography {
                    sx {
                        color = Color("text.secondary")
                        maxWidth = 700.px
                        margin = Margin(0.px, Auto.auto)
                    }
                    +"KOMM comes with built-in plugins and supports custom extensions to meet your specific needs."
                }
            }
            Box {
                sx {
                    marginTop = 6.px
                }
                Plugin {
                    icon = AlignJustify.create {
                        size = 24
                    }
                    title = "Iterable Plugin"
                    description = "Automatically maps collections with different element types, handling all common collection types like List, Set, and Map."
                }
                Plugin {
                    icon = Database.create {
                        size = 24
                    }
                    title = "Exposed Plugin"
                    description = "Support for mapping from Exposed Table Objects (ResultRow) directly to your domain models, simplifying database access."
                }
                Plugin {
                    icon = SquareTerminal.create {
                        size = 24
                    }
                    title = "Enum Plugin"
                    description = "Simplifies mapping between different enum types with support for default values through annotations."
                }
            }
        }
    }
}