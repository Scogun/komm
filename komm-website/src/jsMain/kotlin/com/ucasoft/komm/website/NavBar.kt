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
import react.*
import react.dom.html.ReactHTML.button
import web.cssom.*
import web.html.HTMLDivElement

val NavBar = FC<NavBarProps> {
    val isMobile = useMediaQuery<Theme>({
        it.breakpoints.down(Breakpoint.md)
    })
    var isMobileOpen by useState(false)
    AppBar {
        position = AppBarPosition.fixed
        elevation = 2
        sx {
            backgroundColor = rgb(255, 255, 255, 0.85)
            backdropFilter = blur(10.px)
            boxShadow = "sm".unsafeCast<BoxShadow>()
        }
        Container {
            maxWidth = "lg"
            Toolbar {
                disableGutters = true
                sx {
                    justifyContent = JustifyContent.spaceBetween
                }
                Logo {}
                if (!isMobile) {
                    Box {
                        sx {
                            display = js("{ xs: 'none', md: 'flex' }")
                            alignItems = AlignItems.center
                            gap = 2.px
                        }
                        it.menu.map { item ->
                            Link {
                                component = button
                                variant = "body1"
                                onClick = {
                                    if (item.ref.current != null) {
                                        item.ref.current!!.scrollIntoView(js("{ behavior: 'smooth', block: 'start' }"))
                                    }
                                }
                                sx {
                                    color = Color("text.primary")
                                    asDynamic().`&:hover` = js("{ color: 'primary.main' }")
                                }
                                +item.title
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

external interface NavBarProps : Props {
    var menu: List<NavBarMenu>
}

data class NavBarMenu(val title: String, val ref: RefObject<HTMLDivElement>)