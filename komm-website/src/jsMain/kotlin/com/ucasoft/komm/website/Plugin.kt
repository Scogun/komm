package com.ucasoft.komm.website

import mui.material.Box
import mui.material.Card
import mui.material.CardContent
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.dom.html.ReactHTML.h3
import web.cssom.AlignItems
import web.cssom.Color
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.TextAlign
import web.cssom.number
import web.cssom.pct
import web.cssom.px

val Plugin = FC<PluginProps> {
    Card {
        sx {
            textAlign = TextAlign.center
            padding = 3.px
            height = 100.pct
        }
        Box {
            sx {
                display = Display.flex
                justifyContent = JustifyContent.center
                marginBottom = 2.px
                color = Color("primary.main")
            }
            +it.icon
        }
        Typography {
            variant = TypographyVariant.h5
            component = h3
            gutterBottom = true
            sx {
                color = Color("text.primary")
            }
            +it.title
        }
        Typography {
            variant = TypographyVariant.body1
            sx {
                color = Color("text.secondary")
            }
            +it.description
        }
    }
}

external interface PluginProps : Props {
    var icon: ReactNode
    var title: String
    var description: String
}