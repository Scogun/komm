package com.ucasoft.komm.website.pages.quickStart

import com.ucasoft.komm.website.components.code.CodeTabs
import com.ucasoft.komm.website.pages.BreadCrumb
import com.ucasoft.komm.website.pages.PageContainer
import com.ucasoft.wrappers.lucide.Rocket
import mui.material.Card
import mui.material.CardContent
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.create
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.h3
import react.useState
import web.cssom.*

val QuickStart = FC {

    var codeType by useState(steps.first().codes.first().type)

    PageContainer {
        homePath = "Home"
        breadcrumbs = listOf(BreadCrumb(Rocket.create(), "Quick Start", "/quickstart"))
        Typography {
            variant = TypographyVariant.h1
            component = h1
            gutterBottom = true
            +"Quick Start"
        }
        Typography {
            variant = TypographyVariant.h5
            sx {
                color = Color("text.primary")
                marginBottom = 4.px
                fontWeight = integer(400)
            }
            +"Get KOMM up and running in your Kotlin Multiplatform or JVM project in just a few steps."
        }
        steps.map {
            Card {
                sx {
                    marginBottom = 5.px
                }
                CardContent {
                    Typography {
                        variant = TypographyVariant.h4
                        component = h3
                        gutterBottom = true
                        +it.title
                    }
                    Typography {
                        variant = TypographyVariant.body1
                        sx {
                            marginBottom = 2.px
                            whiteSpace = WhiteSpace.preLine
                            lineHeight = number(1.7)
                        }
                        +it.description
                    }
                    CodeTabs {
                        type = codeType
                        items = it.codes
                        typeChange = { newCode -> codeType = newCode }
                    }
                }
            }
        }
    }
}