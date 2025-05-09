package com.ucasoft.komm.website

import com.ucasoft.wrappers.lucide.GitHub
import com.ucasoft.wrappers.lucide.Library
import com.ucasoft.wrappers.lucide.X
import com.ucasoft.wrappers.lucide.Menu
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.ReactNode
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div
import react.useState
import web.cssom.AlignItems
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.vh

val App = FC {

    var activeTab by useState(0)
    var mobileMenuOpen by useState(false)

    Box {
        sx {
            minHeight = 100.vh
        }
        AppBar {
            position = AppBarPosition.static
            color = AppBarColor.primary
            elevation = 4
            Container {
                Toolbar {
                    sx {
                        display = Display.flex
                        justifyContent = JustifyContent.spaceBetween
                    }
                    Box {
                        sx {
                            display = Display.flex
                            alignItems = AlignItems.center
                        }
                        Library {
                            size = 28
                        }
                        Typography {
                            variant = TypographyVariant.h6
                            component = div
                            sx {
                                maxLines = 1.asDynamic()
                                fontWeight = 700.asDynamic()
                            }
                            +"KOMM"
                        }
                        Typography {
                            variant = TypographyVariant.body2
                            sx {
                                maxLines = 1.asDynamic()
                                display = js("{ xs: 'none', sm: 'block' }")
                            }
                            +"Kotlin Object Multiplatform Mapper"
                        }
                    }

                    Box {
                        sx {
                            display = Display.flex
                        }
                        Tabs {
                            value = activeTab
                            onChange = { _, newIndex -> activeTab = newIndex as Int }
                            textColor = TabsTextColor.inherit
                            indicatorColor = TabsIndicatorColor.secondary
                            listOf("Home", "Documentation", "Examples", "Plugins").forEachIndexed { index, label ->
                                Tab {
                                    value = index
                                    this.label = ReactNode(label)
                                }
                            }
                        }
                    }

                    Box {
                        sx {
                            display = Display.flex
                            alignItems = AlignItems.center
                        }
                        IconButton {
                            color = IconButtonColor.inherit
                            component = ReactHTML.a
                            asDynamic().href = "https://github.com/Scogun/KOMM"
                            asDynamic().target = "_blank"
                            ariaLabel = "GitHub repository"
                            GitHub {
                                size = 20
                            }
                        }
                        IconButton {
                            color = IconButtonColor.inherit
                            onClick = {
                                mobileMenuOpen = !mobileMenuOpen
                            }
                            sx {
                                display = js("{md: 'none'}")
                            }
                            ariaLabel = "Menu"
                            if (mobileMenuOpen) {
                                X {
                                    size = 24
                                }
                            } else {
                                Menu {
                                    size = 24
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}