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
import web.cssom.AlignItems
import web.cssom.Color
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.atrule.color
import web.cssom.number
import web.cssom.px

val Feature = FC<FeatureProps> {
    Card {
        CardContent {
            Box {
                sx {
                    display = Display.inlineFlex
                    alignItems = AlignItems.center
                    justifyContent = JustifyContent.center
                    width = 60.px
                    height = 60.px
                    borderRadius = 2.px
                    backgroundColor = Color("primary.light")
                    opacity = number(0.1)
                    marginBottom = 2.px
                }
                Box {
                    sx {
                        color = Color("primary.main")
                    }
                    +it.icon
                }
            }
            Typography {
                variant = TypographyVariant.h3
                gutterBottom = true
                +it.title
            }
            Typography {
                asDynamic().color = Color("primary.secondary")
                +it.description
            }
        }
    }
}

external interface FeatureProps : Props {
    var description: String
    var icon: ReactNode
    var title: String
}