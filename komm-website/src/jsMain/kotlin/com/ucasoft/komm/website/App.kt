package com.ucasoft.komm.website

import com.ucasoft.komm.website.data.IconItem
import com.ucasoft.komm.website.data.ListPathItem
import com.ucasoft.komm.website.data.PathItem
import com.ucasoft.komm.website.pages.DetailPage
import com.ucasoft.komm.website.pages.annotations.Annotations
import com.ucasoft.komm.website.pages.annotations.annotationData
import com.ucasoft.komm.website.pages.home.HomePage
import com.ucasoft.komm.website.pages.plugins.Plugins
import com.ucasoft.komm.website.pages.plugins.pluginData
import com.ucasoft.komm.website.pages.quickStart.QuickStart
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
import mui.material.useMediaQuery
import mui.material.styles.Theme
import mui.material.styles.ThemeProvider
import mui.material.styles.createTheme
import mui.system.Breakpoint
import mui.system.sx
import react.FC
import react.create
import react.useEffect
import tanstack.react.router.Outlet
import tanstack.react.router.RootRouteOptions
import tanstack.react.router.Route
import tanstack.react.router.RouteOptions
import tanstack.react.router.RouterOptions
import tanstack.react.router.RouterProvider
import tanstack.react.router.createRootRoute
import tanstack.react.router.createRoute
import tanstack.react.router.createRouter
import tanstack.react.router.useLocation
import tanstack.router.core.AnyRoute
import tanstack.router.core.ParamName
import tanstack.router.core.RouteLoaderEntry
import tanstack.router.core.RoutePath
import web.cssom.*
import web.window.window

val appTheme: Theme = createTheme(
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
    PathItem(Rocket.create(), "Quick Start", "/quickstart", QuickStart),
    ListPathItem(
        Tag.create(), "Annotations", "/annotations", Annotations, listOf(
            IconItem(Settings.create { size = 40 }, "@KOMMMap", "Main annotation for marking mapping classes"),
            IconItem(
                Settings.create { size = 40 },
                "@MapFunction",
                "Calls a configured top-level extension function for property conversion"
            ),
            IconItem(
                Settings.create { size = 40 },
                "@MapName",
                "Provides possibility to map properties with different names"
            ),
            IconItem(
                Settings.create { size = 40 },
                "@MapEmbedded",
                "Maps several destination properties from the same nested source property"
            ),
            IconItem(
                Settings.create { size = 40 },
                "@MapConvert",
                "Provides possibility to add additional logic for properties mapping"
            ),
            IconItem(
                Settings.create { size = 40 },
                "@MapDefault",
                "Provides possibility to add default values for orphans properties"
            ),
            IconItem(
                Settings.create { size = 40 },
                "@MapTargetDefault",
                "Defines target property defaults for external destination classes"
            ),
            IconItem(Settings.create { size = 40 }, "@NullSubstitute", "Extends mapping from nullable type properties")
        )
    ),
    ListPathItem(
        Puzzle.create(), "Plugins", "/plugins", Plugins, listOf(
            IconItem(
                ListTree.create { size = 40 },
                "Iterable Plugin",
                "Supports mapping collections with different types of elements, simplifying list transformations."
            ),
            IconItem(
                Database.create { size = 40 },
                "Exposed Plugin",
                "Provides mapping from Exposed Table Objects (ResultRow) for easy database interaction."
            ),
            IconItem(
                Puzzle.create { size = 40 },
                "Enum Plugin",
                "Supports mapping enums from other enums, including default value annotations for robustness."
            )
        )
    ),
    //PathItem(Code.create(), "Examples", "/", HomePage),
)

val detailData = annotationData + pluginData

val App = FC {

    val rootRouter = createRootRoute(
        RootRouteOptions(
            component = Root
        )
    )

    val childRoutes = mutableListOf<AnyRoute>()

    navigationData.take(1).forEach {
        val indexRoute = createRoute(
            RouteOptions(
                getParentRoute = { rootRouter },
                path = RoutePath("/"),
                component = it.component
            )
        )
        childRoutes.add(indexRoute)
    }

    navigationData.drop(1).forEach { item ->
        val firstLineRoute = createRoute(
            RouteOptions(
                getParentRoute = { rootRouter },
                path = RoutePath(item.path),
                component = item.component,
                loader = ({ _: dynamic -> item }).unsafeCast<RouteLoaderEntry>()
            )
        )
        childRoutes.add(firstLineRoute)
    }

    navigationData.drop(1).filterIsInstance<ListPathItem>().forEach { item ->
        val secondLineRoute = createRoute(
            RouteOptions(
                getParentRoute = { rootRouter },
                path = RoutePath("${item.path}/", ParamName("id")),
                component = DetailPage,
                loader = ({ context: dynamic -> detailData.first { it.id == context.params.id } }).unsafeCast<RouteLoaderEntry>()
            )
        )
        childRoutes.add(secondLineRoute)
    }

    rootRouter.addChildren(childRoutes.toTypedArray().unsafeCast<Array<out Route>>())

    val router = createRouter(
        RouterOptions(
            routeTree = rootRouter
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

    val isMobile = useMediaQuery<Theme>({
        it.breakpoints.down(Breakpoint.md)
    })

    Box {
        sx {
            display = Display.flex
            minHeight = 100.vh
            flexDirection = FlexDirection.column
        }
        NavBar {
            menu = navigationData
            this.isMobile = isMobile
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
                    paddingBottom = appTheme.asDynamic().spacing(if (isMobile) 13 else 21).unsafeCast<Length>()
                }
                Outlet {}
            }
            Footer {
                this.isMobile = isMobile
            }
        }
    }
}
