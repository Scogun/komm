package com.ucasoft.komm.website

import js.objects.unsafeJso
import mui.material.Box
import mui.material.CssBaseline
import mui.material.styles.ThemeProvider
import mui.material.styles.createTheme
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
                dark = rgb(104, 66, 208)
                contrastText = rgb(255, 255, 255)
            }
            secondary = unsafeJso {
                main = rgb(30, 136, 229)
            }
            background = unsafeJso {
                default = "#f8f9fa"
                paper = "#ffffff"
            }
            text = unsafeJso {
                primary = rgb(12, 12, 12)
                secondary = rgb(108, 117, 125)
            }
        }
        typography = unsafeJso {
            fontFamily = arrayOf("\"Inter\"",
                "-apple-system",
                "BlinkMacSystemFont",
                "\"Segoe UI\"",
                "Roboto",
                "Oxygen",
                "Ubuntu",
                "Cantarell",
                "\"Open Sans\"",
                "\"Helvetica Neue\"",
                "sans-serif").joinToString(",")
            h1 = unsafeJso {
                fontSize = 3.5.rem
                fontWeight = 800
            }
            h2 = unsafeJso {
                fontSize = 2.5.rem
                fontWeight = 700
                marginBottom = 1.rem
            }
            h3 = unsafeJso {
                fontSize = 1.5.rem
                fontWeight = 600
                marginBottom = 1.rem
            }
            body1 = unsafeJso {
                fontSize = 1.2.rem
            }
        }
        components = unsafeJso {
            MuiButton = unsafeJso {
                styleOverrides = unsafeJso {
                    root = unsafeJso {
                        borderRadius = 6.px
                        fontWeight = 600
                        padding = "10px 20px"
                        textTransform = "none"
                        transition = "all 0.3s"
                        boxShadow = "none"
                    }
                }
            }
            MuiCard = unsafeJso {
                styleOverrides = unsafeJso {
                    root = unsafeJso {
                        borderRadius = 8.px
                        transition = "transform 0.3s, box-shadow 0.3s"
                    }
                }
            }
        }
    }
)

val App = FC {
    ThemeProvider {
        theme = appTheme
        CssBaseline {}
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
                Features {}
                CodeExamples {}
                Targets {}
                Plugins {}
                Installation {}
                Footer {}
            }
        }
    }
}