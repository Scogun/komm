package com.ucasoft.komm.website

import mui.material.Box
import mui.material.Container
import mui.material.Divider
import mui.material.Grid
import mui.material.List
import mui.material.ListItem
import mui.material.ListItemText
import mui.material.Typography
import mui.material.TypographyAlign
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.FC
import react.create
import react.dom.html.ReactHTML.a
import web.cssom.Color
import web.cssom.Padding
import web.cssom.px
import web.cssom.rgb
import kotlin.js.Date

val footerLinks = mapOf(
    "Resources" to mapOf(
        "https://github.com/Scogun/komm" to "GitHub Repository",
        "https://github.com/Scogun/komm/issues" to "Issue Tracker",
        "https://github.com/Scogun/komm/releases" to "Release Notes",
    ),
    "Links" to mapOf(
        "https://kotlinlang.org/" to "Kotlin Programming Language",
        "https://kotlinlang.org/docs/ksp-overview.html" to "Kotlin Symbol Processing",
        "https://kotlinlang.org/docs/multiplatform.html" to "Kotlin Multiplatform",
    )
)

val Footer = FC {
    Box {
        sx {
            backgroundColor = Color("text.primary")
            color = Color("white")
            padding = Padding(6.px, 0.px)
        }
        Container {
            Grid {
                container = true
                spacing = responsive(4)
                sx {
                    marginBottom = 6.px
                }
                Grid {
                    item = true
                    asDynamic().xs = responsive(12)
                    asDynamic().md = responsive(4)
                    Box {
                        sx {
                            marginBottom = 3.px
                        }
                        Logo {}
                    }
                    Typography {
                        variant = TypographyVariant.body2
                        sx {
                            color = rgb(255, 255, 255, 0.7)
                        }
                        +"KOMM is a powerful Kotlin Symbol Processing (KSP) based library for mapping between object models in Kotlin Multiplatform projects."
                    }
                }
                footerLinks.map {
                    Grid {
                        item = true
                        asDynamic().xs = responsive(12)
                        asDynamic().sm = responsive(6)
                        asDynamic().md = responsive(4)
                        Typography {
                            variant = TypographyVariant.h6
                            sx {
                                marginBottom = 3.px
                            }
                            +it.key
                        }
                        List {
                            disablePadding = true
                            it.value.map {
                                ListItem {
                                    disableGutters = true
                                    disablePadding = true
                                    ListItemText {
                                        primary = Typography.create {
                                            variant = TypographyVariant.body2
                                            component = a
                                            asDynamic().href = it.key
                                            asDynamic().target = "_blank"
                                            sx {
                                                color = rgb(255, 255, 255, 0.7)

                                            }
                                            +it.value
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Divider {
                sx {
                    borderColor = rgb(255, 255, 255, 0.1)
                    marginBottom = 3.px
                }
            }
            Typography {
                variant = TypographyVariant.body2
                align = TypographyAlign.center
                sx {
                    color = rgb(255, 255, 255, 0.5)
                }
                +"© ${Date().getFullYear()} KOMM. Released under the Apache License."
            }
        }
    }
}