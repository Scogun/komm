package com.ucasoft.komm.website.pages

import com.ucasoft.komm.website.DetailItem
import com.ucasoft.wrappers.lucide.Puzzle
import com.ucasoft.wrappers.lucide.Tag
import mui.material.Card
import mui.material.CardContent
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.create
import react.dom.html.ReactHTML.h1
import react.router.useLoaderData
import react.useState
import web.cssom.AlignItems
import web.cssom.Display
import web.cssom.WhiteSpace
import web.cssom.px

val DetailPage = FC {

    val item = useLoaderData().unsafeCast<DetailItem>()
    val parentIcon = if (item.title.endsWith("Plugin")) Puzzle.create() else Tag.create()
    val parentTitle = if (item.title.endsWith("Plugin")) "Plugins" else "Annotations"

    val breadCrumbs = listOf(
        BreadCrumb(parentIcon, parentTitle, "/${parentTitle.lowercase()}"),
        BreadCrumb(item.icon, item.title, item.title)
    )

    var codeType by useState(item.codes.firstOrNull()?.type)

    PageContainer {
        homePath = "Home"
        breadcrumbs = breadCrumbs
        Typography {
            variant = TypographyVariant.h2
            component = h1
            gutterBottom = true
            sx {
                display = Display.flex
                alignItems = AlignItems.center
            }
            +item.title
        }
        Card {
            CardContent {
                Typography {
                    variant = TypographyVariant.h4
                    gutterBottom = true
                    +"Details"
                }
                Typography {
                    variant = TypographyVariant.body1
                    sx {
                        whiteSpace = WhiteSpace.preLine
                        lineHeight = 1.7.px
                    }
                    +item.description
                }
            }
        }
        if (item.codes.isNotEmpty()) {
            Card {
                sx {
                    marginTop = 5.px
                }
                CardContent {
                    Typography {
                        variant = TypographyVariant.h4
                        gutterBottom = true
                        +"Installation"
                    }
                    CodeTabs {
                        type = codeType
                        items = item.codes
                        typeChange = { newCode -> codeType = newCode }
                    }
                }
            }
        }
    }
}