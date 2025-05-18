package com.ucasoft.komm.website

import mui.material.Avatar
import mui.material.Card
import mui.material.CardContent
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import web.cssom.Color
import web.cssom.px
import web.cssom.rgb

val Feature = FC<FeatureProps> {
    Card {
        CardContent {
            Avatar {
                sx {
                    backgroundColor = rgb(127, 82, 255, 0.1)
                    color = Color("primary.main")
                    width = 60.px
                    height = 60.px
                    marginBottom = 2.px
                }
                +it.icon
            }
            Typography {
                variant = TypographyVariant.h3
                gutterBottom = true
                +it.title
            }
            Typography {
                asDynamic().color = Color("text.secondary")
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