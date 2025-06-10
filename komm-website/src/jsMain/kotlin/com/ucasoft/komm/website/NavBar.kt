package com.ucasoft.komm.website

import com.ucasoft.komm.website.data.PathItem
import com.ucasoft.wrappers.lucide.GitHub
import com.ucasoft.wrappers.lucide.Menu
import js.objects.unsafeJso
import mui.material.*
import mui.material.styles.Theme
import mui.material.styles.useTheme
import mui.system.sx
import react.*
import react.router.dom.Link
import web.cssom.*

external interface NavBarProps : Props {
    var menu: List<PathItem>
    var isMobile: Boolean
}

private external interface NavDrawerProps : NavBarProps {
    var isOpen: Boolean
    var onChoose: (PathItem) -> Unit
}

val NavBar = FC<NavBarProps> {
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
                if (it.isMobile) {
                    IconButton {
                        color = IconButtonColor.inherit
                        ariaLabel = "open drawer"
                        edge = IconButtonEdge.start
                        onClick = { isMobileOpen = !isMobileOpen }
                        Menu {}
                    }
                }
                Logo {}
                if (!it.isMobile) {
                    Box {
                        sx {
                            display = Display.flex
                            alignItems = AlignItems.center
                            gap = 2.px
                            marginLeft = 4.px
                        }
                        it.menu.map { item ->
                            Button {
                                component = Link
                                asDynamic().to = item.path
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
                            component = Link
                            asDynamic().to = item.path
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