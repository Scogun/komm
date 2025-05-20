package com.ucasoft.komm.website

import com.ucasoft.wrappers.lucide.GitHub
import com.ucasoft.wrappers.lucide.Menu
import js.objects.unsafeJso
import mui.material.*
import mui.material.styles.Theme
import mui.material.styles.useTheme
import mui.system.Breakpoint
import mui.system.sx
import mui.system.useMediaQuery
import react.*
import web.cssom.*
import web.html.HTMLDivElement

data class NavBarMenu(val icon: ReactNode, val title: String, val ref: RefObject<HTMLDivElement>)

external interface NavBarProps : Props {
    var menu: List<NavBarMenu>
}

private external interface NavDrawerProps : NavBarProps {
    var isOpen: Boolean
    var onChoose: (NavBarMenu) -> Unit
}

val NavBar = FC<NavBarProps> {
    val isMobile = useMediaQuery<Theme>({
        it.breakpoints.down(Breakpoint.md)
    })
    var isMobileOpen by useState(false)
    val theme = useTheme<Theme>()
    AppBar {
        position = AppBarPosition.fixed
        sx {
            zIndex = integer(theme.zIndex.drawer.toInt() + 1)
        }
        Container {
            maxWidth = "xl"
            Toolbar {
                disableGutters = true
                if (isMobile) {
                    IconButton {
                        color = IconButtonColor.inherit
                        ariaLabel = "open drawer"
                        edge = IconButtonEdge.start
                        onClick = { isMobileOpen = !isMobileOpen }
                        Menu {}
                    }
                }
                Logo {}
                if (!isMobile) {
                    Box {
                        sx {
                            display = Display.flex
                            alignItems = AlignItems.center
                            gap = 2.px
                            marginLeft = 4.px
                        }
                        it.menu.map { item ->
                            Button {
                                onClick = { navClickHandler(item) }
                                sx {
                                    color = Color("text.primary")
                                    hover { color = Color("primary.main") }
                                }
                                startIcon = item.icon
                                +item.title
                            }
                        }
                    }
                    Box {
                        sx {
                            flexGrow = number(1.0)
                        }
                    }
                    GitHubButton {
                        variant = ButtonVariant.contained
                    }
                }
            }
        }
    }
    NavDrawer {
        menu = it.menu
        isOpen = isMobileOpen
        onChoose = {
            navClickHandler(it)
            isMobileOpen = false
        }
    }
}

private val NavDrawer = FC<NavDrawerProps> { d ->
    Drawer {
        variant = DrawerVariant.temporary
        open = d.isOpen
        ModalProps = unsafeJso {
            keepMounted = true
        }
        sx {
            display = Display.block
            asDynamic()["& .MuiDrawer-paper"] = unsafeJso {
                boxSizing = BoxSizing.borderBox
                width = 240.px
            }
        }
        Box {
            sx {
                minWidth = 225.px
            }
            Divider {
                sx {
                    marginTop = 70.px
                }
            }
            List {
                d.menu.map { item ->
                    ListItem {
                        disablePadding = true
                        ListItemButton {
                            onClick = { d.onChoose(item) }
                            ListItemIcon {
                                sx {
                                    minWidth = 40.px
                                    color = Color("primary.main")
                                }
                                +item.icon
                            }
                            ListItemText {
                                primary = ReactNode(item.title)
                            }
                        }
                    }
                }
            }
            Divider {}
            Box {
                sx {
                    padding = 2.px
                }
                GitHubButton {
                    fullWidth = true
                    variant = ButtonVariant.outlined
                    color = ButtonColor.primary
                }
            }
        }
    }
}

private val navClickHandler: (item: NavBarMenu) -> Unit = {
    if (it.ref.current != null) {
        it.ref.current!!.scrollIntoView(js("{ behavior: 'smooth', block: 'start' }"))
    }
}

private val GitHubButton = FC<ButtonProps> {
    Button {
        fullWidth = it.fullWidth
        variant = it.variant
        color = it.color
        href = "https://github.com/Scogun/komm"
        asDynamic().target = "_blank"
        startIcon = GitHub.create {
            size = 18
        }
        rel = "noopener noreferre"
        sx {
            display = Display.inlineFlex
        }
        +"GitHub"
    }
}