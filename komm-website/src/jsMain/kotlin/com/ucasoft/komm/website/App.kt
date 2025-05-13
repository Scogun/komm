package com.ucasoft.komm.website

import js.objects.unsafeJso
import mui.material.Box
import mui.material.CssBaseline
import mui.material.styles.ThemeProvider
import mui.material.styles.createTheme
import mui.system.responsive
import mui.system.sx
import react.FC
import web.cssom.Display
import web.cssom.FlexDirection
import web.cssom.number
import web.cssom.px
import web.cssom.rem
import web.cssom.rgb
import web.cssom.vh

val appTheme = createTheme(
    unsafeJso {
        palette = unsafeJso {
            primary = unsafeJso {
                main = rgb(127, 82, 255)
                light = rgb(157, 122, 255)
                dark = rgb(104, 66, 208)
                contrastText = rgb(255, 255, 255)
            }
            secondary = unsafeJso {
                main = rgb(30, 136, 229)
                light = rgb(74, 163, 243)
                dark = rgb(22, 103, 180)
                contrastText = rgb(255, 255, 255)
            }
            background = unsafeJso {
                default = "#f8f9fa"
                paper = "#ffffff"
            }
        }
        typography = unsafeJso {
            fontFamily = arrayOf("Inter",
                "-apple-system",
                "BlinkMacSystemFont",
                "Segoe UI",
                "Roboto",
                "Helvetica Neue",
                "Arial",
                "sans-serif").joinToString(",")
            h1 = unsafeJso {
                fontSize = unsafeJso {
                    xs = 2.5.rem
                    md = 3.rem
                }
                fontWeight = 800
            }
            h2 = unsafeJso {
                fontSize = 2.5.rem
                fontWeight = 700
                marginBottom = 16.px
            }
            h3 = unsafeJso {
                fontSize = 1.5.rem
                fontWeight = 600
            }
            button = unsafeJso {
                fontWeight = 600
                textTransform = "none"
            }
        }
        components = unsafeJso {
            MuiButton = unsafeJso {
                styleOverrides = unsafeJso {
                    root = unsafeJso {
                        borderRadius = 0.px
                        padding = "10px 20px"
                    }
                }
            }
            MuiCard = unsafeJso {
                styleOverrides = unsafeJso {
                    root = unsafeJso {
                        borderRadius = 8.px
                        boxShadow = "0px 4px 10px rgba(0, 0, 0, 0.05)"
                    }
                }
            }
            MuiPaper = unsafeJso {
                styleOverrides = unsafeJso {
                    root = unsafeJso {
                        borderRadius = 8.px
                    }
                }
            }
        }
    }
)

val App = FC {
    ThemeProvider {
        theme = appTheme
        CssBaseline
        Box {
            sx {
                display = Display.flex
                flexDirection = FlexDirection.column
                minHeight = 100.vh
            }
            NavBar {}
            Box {
                asDynamic().component = "main"
                sx {
                    flexGrow = number(1.0)
                }
                Hero {}
            }
        }
    }
}