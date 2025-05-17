package com.ucasoft.komm.website

import mui.material.Box
import mui.material.Card
import mui.material.CardContent
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.Props
import web.cssom.AlignItems
import web.cssom.BoxShadow
import web.cssom.Color
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.TextAlign
import web.cssom.pct
import web.cssom.px

val Target = FC<TargetProps> {
    Card {
        sx {
            textAlign = TextAlign.center
            backgroundColor = Color("background.default")
        }
        CardContent {
            sx {
                padding = 3.px
            }
            Box {
                sx {
                    display = Display.inlineFlex
                    alignItems = AlignItems.center
                    justifyContent = JustifyContent.center
                    width = 80.px
                    height = 80.px
                    borderRadius = 50.pct
                    background = Color("background.paper")
                    boxShadow = BoxShadow(1.px, 1.px, Color.currentcolor)
                    marginBottom = 2.px
                }
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
    var title: String
    var description: String
}