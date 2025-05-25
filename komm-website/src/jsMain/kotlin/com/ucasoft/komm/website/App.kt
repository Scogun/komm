package com.ucasoft.komm.website

import com.ucasoft.komm.website.pages.DetailPage
import com.ucasoft.komm.website.pages.annotations.Annotations
import com.ucasoft.komm.website.pages.home.HomePage
import com.ucasoft.komm.website.pages.plugins.Plugins
import com.ucasoft.wrappers.lucide.Code
import com.ucasoft.wrappers.lucide.Database
import com.ucasoft.wrappers.lucide.House
import com.ucasoft.wrappers.lucide.ListTree
import com.ucasoft.wrappers.lucide.Puzzle
import com.ucasoft.wrappers.lucide.Rocket
import com.ucasoft.wrappers.lucide.Settings
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
import remix.run.router.LoaderLike
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

val navigationData = listOf(
    PathItem(House.create(), "Home", "/", HomePage),
    PathItem(Rocket.create(), "Quick Start",  "/", HomePage),
    ListPathItem(Tag.create(), "Annotations", "/annotations", Annotations, listOf(
        IconItem(Settings.create { size = 40 }, "@KOMMMap", "Main annotation for marking mapping classes"),
        IconItem(Settings.create { size = 40 }, "@MapName", "Provides possibility to map properties with different names"),
        IconItem(Settings.create { size = 40 }, "@MapConvert", "Provides possibility to add additional logic for properties mapping"),
        IconItem(Settings.create { size = 40 }, "@MapDefault", "Provides possibility to add default values for orphans properties"),
        IconItem(Settings.create { size = 40 }, "@NullSubstitute", "Extends mapping from nullable type properties")
    )),
    ListPathItem(Puzzle.create(), "Plugins", "/plugins", Plugins, listOf(
        IconItem(ListTree.create { size = 40 }, "Iterable Plugin", "Supports mapping collections with different types of elements, simplifying list transformations."),
        IconItem(Database.create { size = 40 }, "Exposed Plugin", "Provides mapping from Exposed Table Objects (ResultRow) for easy database interaction."),
        IconItem(Puzzle.create { size = 40 }, "Enum Plugin", "Supports mapping enums from other enums, including default value annotations for robustness.")
    )),
    PathItem(Code.create(), "Examples", "/", HomePage),
)

val detailData = listOf(
    DetailItem(ListTree.create(), "Iterable Plugin"),
    DetailItem(Database.create(), "Exposed Plugin"),
    DetailItem(Puzzle.create(), "Enum Plugin"),
    DetailItem(Settings.create(), "@KOMMMap"),
    DetailItem(Settings.create(), "@MapName"),
    DetailItem(Settings.create(), "@MapConvert"),
    DetailItem(Settings.create(), "@MapDefault"),
    DetailItem(Settings.create(), "@NullSubstitute")
)

val App = FC {

    val router = createBrowserRouter(
        arrayOf(
            RouteObject(
                path = "/",
                Component = Root,
                children = mutableListOf<RouteObject>().apply {
                    addAll(navigationData.take(1).map {
                        RouteObject(
                            index = true,
                            Component = it.component
                        )
                    })
                    addAll(navigationData.drop(1).map {
                        RouteObject(
                            path = it.path,
                            Component = it.component,
                            loader = {
                                it
                            }.unsafeCast<LoaderLike>()
                        )
                    })
                    addAll(
                    navigationData.drop(1).filterIsInstance<ListPathItem>().map {
                        RouteObject(
                            path = "${it.path}/:id",
                            Component = DetailPage,
                            loader = { context: dynamic ->
                                detailData.first { item -> item.id == context.params.id }
                            }.unsafeCast<LoaderLike>()
                        )
                    })
                }.toTypedArray()
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
            menu = navigationData
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