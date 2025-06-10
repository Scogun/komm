package com.ucasoft.komm.website.pages.home

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
import web.cssom.*

val Feature = FC<FeatureProps> {
    Card {
        sx {
            padding = 2.5.px
            height = 100.pct
        }
        CardContent {
            Box {
                sx {
                    display = Display.flex
                    alignItems = AlignItems.center
                    marginBottom = 2.px
                }
                Box {
                    sx {
                        color = Color("primary.main")
                        marginRight = 1.5.px
                        display = Display.inlineFlex
                    }
                    +it.icon
                }
                Typography {
                    variant = TypographyVariant.h6
                    component = h3
                    sx {
                        color = Color("text.primary")
                    }
                    +it.title
                }
            }
            Typography {
                variant = TypographyVariant.body2
                sx {
                    color = Color("text.secondary")
                }
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