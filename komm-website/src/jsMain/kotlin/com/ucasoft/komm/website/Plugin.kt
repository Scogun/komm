package com.ucasoft.komm.website

import mui.material.Card
import mui.material.CardContent
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import web.cssom.AlignItems
import web.cssom.Color
import web.cssom.Display
import web.cssom.number
import web.cssom.px

val Plugin = FC<PluginProps> {
    Card {
        sx {
            marginBottom = 2.px
        }
        CardContent {
            sx {
                padding = 3.px
            }
            Typography {
                variant = TypographyVariant.h3
                sx {
                    color = Color("primary.main")
                    display = Display.flex
                    alignItems = AlignItems.center
                    gap = 1.5.px
                    marginBottom = 2.px
                }
                +it.icon
                +it.title
            }
            Typography {
                sx {
                    color = Color("text.secondary")
                }
                +it.description
            }
        }
    }
}

external interface PluginProps : Props {
    var icon: ReactNode
    var title: String
    var description: String
}