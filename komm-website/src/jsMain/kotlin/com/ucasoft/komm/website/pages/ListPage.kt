package com.ucasoft.komm.website.pages

import com.ucasoft.komm.website.IconItem
import mui.material.Divider
import mui.material.List
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.material.Paper
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.span
import react.router.dom.Link
import web.cssom.Color
import web.cssom.px

external interface ListPageProps : Props {
    var icon: ReactNode
    var title: String
    var description: String?
    var path: String?
    var items: List<IconItem>
}

val ListPage = FC<ListPageProps> {

    val decapitateTitle = it.title.replaceFirstChar { it.lowercase() }

    PageContainer {
        homePath = "Home"
        breadcrumbs = listOf(BreadCrumb(it.icon, it.title, "/${if (it.path != null) it.path else decapitateTitle}"))
        Typography {
            variant = TypographyVariant.h2
            gutterBottom = true
            +"Plugins"
        }
        Typography {
            variant = TypographyVariant.body1
            sx {
                color = Color("text.secondary")
                marginBottom = 4.px
            }
            if (it.description != null) {
                +it.description
            } else {
                +"Explore the available $decapitateTitle provided by KOMM. Click on an item to see detailed information and usage examples."
            }
        }
        Paper {
            elevation = 2
            List {
                it.items.mapIndexed { index, plugin ->
                    ListItem {
                        disablePadding = true
                        ListItemButton {
                            component = Link
                            asDynamic().to = ""
                            ListItemIcon {
                                sx {
                                    color = Color("primary.main")
                                    minWidth = 40.px
                                }
                                +plugin.icon
                            }
                            ListItemText {
                                primary = Typography.create {
                                    variant = TypographyVariant.h6
                                    component = span
                                    +plugin.title
                                }
                                secondary = ReactNode(plugin.description)
                            }
                        }
                    }
                    if (index < it.items.size - 1) {
                        Divider {
                            component = li
                        }
                    }
                }
            }
        }
    }
}