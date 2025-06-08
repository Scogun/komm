package com.ucasoft.komm.website.pages

import com.ucasoft.komm.website.components.code.CodeTabs
import com.ucasoft.komm.website.data.DetailItem
import com.ucasoft.wrappers.lucide.Puzzle
import com.ucasoft.wrappers.lucide.Tag
import com.ucasoft.wrappers.`react-syntax-highlighter`.SyntaxHighlighter
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
import web.cssom.number
import web.cssom.px

val DetailPage = FC {

    val item = useLoaderData().unsafeCast<DetailItem>()
    val parentIcon = if (item.title.endsWith("Plugin")) Puzzle.create() else Tag.create()
    val parentTitle = if (item.title.endsWith("Plugin")) "Plugins" else "Annotations"

    val breadCrumbs = listOf(
        BreadCrumb(parentIcon, parentTitle, "/${parentTitle.lowercase()}"),
        BreadCrumb(item.icon, item.title, item.title)
    )

    var codeType by useState(item.steps.firstOrNull()?.codes?.firstOrNull()?.type)

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
        item.steps.groupBy { it.title }.map {
            Card {
                sx {
                    marginTop = 5.px
                }
                CardContent {
                    Typography {
                        variant = TypographyVariant.h4
                        gutterBottom = true
                        +it.key
                    }
                    it.value.map {
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
}