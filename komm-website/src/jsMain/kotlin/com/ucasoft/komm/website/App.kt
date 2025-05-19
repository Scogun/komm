package com.ucasoft.komm.website

import js.objects.unsafeJso
import mui.material.Box
import mui.material.CssBaseline
import mui.material.styles.ThemeProvider
import mui.material.styles.createTheme
import react.FC
import react.useRef
import web.cssom.px
import web.cssom.rgb
import web.html.HTMLDivElement

val appTheme = createTheme(
    unsafeJso {
        palette = unsafeJso {
            primary = unsafeJso {
                main = rgb(102, 126, 234)
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
            }
            h2 = unsafeJso {
                fontWeight = 700
            }
            h3 = unsafeJso {
                fontWeight = 700
            }
            h4 = unsafeJso {
                fontWeight = 700
            }
            h5 = unsafeJso {
                fontWeight = 600
            }
            h6 = unsafeJso {
                fontWeight = 600
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
                        asDynamic().`&:hover` = unsafeJso {
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
        }
    }
)

val App = FC {

    val featuresRef = useRef<HTMLDivElement>(null)
    val targetsRef = useRef<HTMLDivElement>(null)
    val pluginsRef = useRef<HTMLDivElement>(null)
    val installationRef = useRef<HTMLDivElement>(null)

    ThemeProvider {
        theme = appTheme
        CssBaseline {}
        NavBar {
            menu = listOf(
                NavBarMenu("Features", featuresRef),
                NavBarMenu("Targets", targetsRef),
                NavBarMenu("Plugins", pluginsRef),
                NavBarMenu("Installation", installationRef),
            )
        }
        Box {
            asDynamic().component = "main"
            Hero {}
            Features {
                ref = featuresRef
            }
            Targets {
                ref = targetsRef
            }
            Plugins {
                ref = pluginsRef
            }
            Installation {
                ref = installationRef
            }
            //CodeExamples {}
            Footer {}
        }
    }
}