package com.ucasoft.komm.website

import com.ucasoft.komm.website.pages.home.HomePage
import com.ucasoft.komm.website.pages.plugins.Plugins
import com.ucasoft.wrappers.lucide.Code
import com.ucasoft.wrappers.lucide.Puzzle
import com.ucasoft.wrappers.lucide.Rocket
import com.ucasoft.wrappers.lucide.Tag
import js.objects.unsafeJso
import mui.material.Box
import mui.material.CssBaseline
import mui.material.styles.ThemeProvider
import mui.material.styles.createTheme
import mui.system.sx
import react.FC
import react.create
import react.router.Outlet
import react.router.RouteObject
import react.router.dom.RouterProvider
import react.router.dom.createBrowserRouter
import react.router.useLocation
import react.useEffect
import web.cssom.*
import web.window.window

val appTheme = createTheme(
    unsafeJso {
        palette = unsafeJso {
            primary = unsafeJso {
                main = rgb(102, 126, 234)
                light = rgb(132, 151, 238)
            }
            secondary = unsafeJso {
                main = rgb(118, 75, 162)
            }
            background = unsafeJso {
                default = "#f8fafc"
                paper = "#ffffff"
            }
            text = unsafeJso {
                primary = rgb(55, 65, 81)
                secondary = rgb(107, 114, 128)
            }
        }
        typography = unsafeJso {
            fontFamily = arrayOf("Inter", "sans-serif").joinToString(",")
            h1 = unsafeJso {
                fontWeight = 700
                fontSize = 2.5.rem
                `@media (min-width:600px)` = unsafeJso {
                    fontSize = 3.rem
                }
                `@media (min-width:900px)` = unsafeJso {
                    fontSize = 3.5.rem
                }
            }
            h2 = unsafeJso {
                fontWeight = 700
                fontSize = 2.rem
                `@media (min-width:600px)` = unsafeJso {
                    fontSize = 2.5.rem
                }
            }
            h3 = unsafeJso {
                fontWeight = 700
                fontSize = 1.75.rem
                `@media (min-width:600px)` = unsafeJso {
                    fontSize = 2.rem
                }
            }
            h4 = unsafeJso {
                fontWeight = 600
                fontSize = 1.5.rem
                `@media (min-width:600px)` = unsafeJso {
                    fontSize = 1.75.rem
                }
            }
            h5 = unsafeJso {
                fontWeight = 600
                fontSize = 1.25.rem
            }
            h6 = unsafeJso {
                fontWeight = 600
                fontSize = 1.1.rem
            }
        }
        components = unsafeJso {
            MuiButton = unsafeJso {
                styleOverrides = unsafeJso {
                    root = unsafeJso {
                        borderRadius = 8.px
                        textTransform = "none"
                    }
                }
            }
            MuiCard = unsafeJso {
                styleOverrides = unsafeJso {
                    root = unsafeJso {
                        borderRadius = 12.px
                        transition = "box-shadow 0.3s ease-in-out, transform 0.3s ease-in-out"
                        `&:hover` = unsafeJso {
                            boxShadow = "0px 10px 20px rgba(0, 0, 0, 0.1)"
                            transform = "translateY(-4px)"
                        }
                    }
                }
            }
            MuiPaper = unsafeJso {
                styleOverrides = unsafeJso {
                    root = unsafeJso {
                        borderRadius = 12.px
                    }
                }
            }
            MuiAppBar = unsafeJso {
                styleOverrides = unsafeJso {
                    root = unsafeJso {
                        backgroundColor = rgb(255, 255, 255, 0.85)
                        backdropFilter = blur(10.px)
                        boxShadow = 1
                    }
                }
            }
        }
    }
)

val App = FC {

    val router = createBrowserRouter(
        arrayOf(
            RouteObject(
                path = "/",
                Component = Root,
                children = arrayOf(
                    RouteObject(
                        index = true,
                        Component = HomePage,
                    ),
                    RouteObject(
                        path = "/plugins",
                        Component = Plugins
                    )
                )
            )
        )
    )

    ThemeProvider {
        theme = appTheme
        CssBaseline {}
        RouterProvider {
            this.router = router
        }
    }
}

private val Root = FC {

    val path = useLocation()

    useEffect(path) {
        window.scrollTo(0.0, 0.0)
    }

    Box {
        sx {
            display = Display.flex
            minHeight = 100.vh
            flexDirection = FlexDirection.column
        }
        NavBar {
            menu = listOf(
                NavBarMenu(Rocket.create(), "Features",  "/"),
                NavBarMenu(Tag.create(), "Annotations", "/"),
                NavBarMenu(Puzzle.create(), "Plugins", "/plugins"),
                NavBarMenu(Code.create(), "Installation", "/"),
            )
        }
        Box {
            asDynamic().component = "main"
            sx {
                flexGrow = number(1.0)
                paddingTop = 64.px
                display = Display.flex
                flexDirection = FlexDirection.column
            }
            Box {
                sx {
                    flexGrow = number(1.0)
                    paddingBottom = appTheme.spacing(35)
                }
                Outlet {}
            }
            Footer {}
        }
    }
}