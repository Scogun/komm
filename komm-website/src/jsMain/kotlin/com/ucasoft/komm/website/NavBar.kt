package com.ucasoft.komm.website

import com.ucasoft.wrappers.lucide.GitHub
import com.ucasoft.wrappers.lucide.Menu
import mui.material.*
import mui.material.StackDirection.Companion.row
import mui.material.styles.Theme
import mui.system.Breakpoint
import mui.system.responsive
import mui.system.sx
import mui.system.useMediaQuery
import react.FC
import react.create
import react.useState
import web.cssom.JustifyContent

val NavBar = FC {
    val isMobile = useMediaQuery<Theme>({
        it.breakpoints.down(Breakpoint.md)
    })
    var isMobileOpen by useState(false)
    val menuItems = listOf("Features", "Examples", "Targets", "Plugins", "Installation")
    AppBar {
        position = AppBarPosition.sticky
        color = AppBarColor.default
        elevation = 1
        Container {
            Toolbar {
                sx {
                    justifyContent = JustifyContent.spaceBetween
                }
                Logo {}
                if (!isMobile) {
                    Stack {
                        direction = responsive(row)
                        spacing = responsive(3)
                        menuItems.map {
                            Button {
                                color = ButtonColor.inherit
                                onClick = {}
                                +it
                            }
                        }
                    }
                }
                Stack {
                    direction = responsive(row)
                    spacing = responsive(2)
                    Button {
                        href = "https://github.com/Scogun/komm"
                        asDynamic().target = "_blank"
                        variant = ButtonVariant.outlined
                        startIcon = GitHub.create {
                            size = 18
                        }
                        sx {
                            display = js("{ xs: 'none', sm: 'flex' }")
                        }
                        +"GitHub"
                    }
                    Button {
                        color = ButtonColor.primary
                        variant = ButtonVariant.contained
                        onClick = {}
                        sx {
                            display = js("{ xs: 'none', sm: 'flex' }")
                        }
                        +"Get Started"
                    }
                    if (isMobile) {
                        IconButton {
                            color = IconButtonColor.inherit
                            ariaLabel = "open drawer"
                            edge = IconButtonEdge.end
                            onClick = { isMobileOpen = !isMobileOpen }
                            Menu {}
                        }
                    }
                }
            }
        }
    }
}