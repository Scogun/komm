package com.ucasoft.komm.website.pages.plugins

import com.ucasoft.komm.website.IconItem
import com.ucasoft.komm.website.pages.BreadCrumb
import com.ucasoft.komm.website.pages.PageContainer
import com.ucasoft.wrappers.lucide.Database
import com.ucasoft.wrappers.lucide.ListTree
import com.ucasoft.wrappers.lucide.Puzzle
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
import react.ReactNode
import react.create
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.span
import react.router.dom.Link
import web.cssom.Color
import web.cssom.px

val plugins = listOf(
    IconItem(ListTree.create { size = 40 }, "Iterable Plugin", "Supports mapping collections with different types of elements, simplifying list transformations."),
    IconItem(Database.create { size = 40 }, "Exposed Plugin", "Provides mapping from Exposed Table Objects (ResultRow) for easy database interaction."),
    IconItem(Puzzle.create { size = 40 }, "Enum Plugin", "Supports mapping enums from other enums, including default value annotations for robustness.")
)

val Plugins = FC {
    PageContainer {
        homePath = "Home"
        breadcrumbs = listOf(BreadCrumb(Puzzle.create(), "Plugins", "/plugins"))
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
            +"Explore the available plugins provided by KOMM. Click on an item to see detailed information and usage examples."
        }
        Paper {
            elevation = 2
            List {
                plugins.mapIndexed { index, plugin ->
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
                    if (index < plugins.size - 1) {
                        Divider {
                            component = li
                        }
                    }
                }
            }
        }
    }
}