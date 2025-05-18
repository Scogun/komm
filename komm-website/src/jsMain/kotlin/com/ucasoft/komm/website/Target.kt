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
import web.cssom.*

val Target = FC<TargetProps> {
    Card {
        CardContent {
            sx {
                textAlign = TextAlign.center
            }
            Avatar {
                sx {
                    backgroundColor = Color("background.paper")
                    width = 80.px
                    height = 80.px
                    margin = Margin(0.px, Auto.auto, 2.px)
                    boxShadow = BoxShadow(0.px, 4.px, 10.px, rgb(0, 0, 0, 0.1))
                }
                +it.icon
            }
            Typography {
                variant = TypographyVariant.h3
                gutterBottom = true
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

external interface TargetProps: Props {
    var icon: ReactNode
    var title: String
    var description: String
}