package com.ucasoft.komm.website

import com.ucasoft.wrappers.lucide.Database
import com.ucasoft.wrappers.lucide.Layers
import com.ucasoft.wrappers.lucide.Shield
import com.ucasoft.wrappers.lucide.SlidersVertical
import com.ucasoft.wrappers.lucide.SquareTerminal
import com.ucasoft.wrappers.lucide.Zap
import mui.material.Box
import mui.material.Container
import mui.material.Grid
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.FC
import react.create
import web.cssom.Auto
import web.cssom.Color
import web.cssom.Margin
import web.cssom.Padding
import web.cssom.TextAlign
import web.cssom.px

val Features = FC {
    Box {
        sx {
            padding = Padding(10.px, 0.px)
        }
        Container {
            Box {
                sx {
                    textAlign = TextAlign.center
                    marginBottom = 6.px
                }
                Typography {
                    variant = TypographyVariant.h2
                    +"Powerful Features"
                }
                Typography {
                    sx {
                        color = Color("text.secondary")
                        maxWidth = 700.px
                        margin = Margin(0.px, Auto.auto)
                    }
                    +"KOMM provides a versatile and flexible mapping solution for Kotlin Multiplatform projects."
                }
            }
            Grid {
                container = true
                spacing = responsive(3)
                Grid {
                    item = true
                    asDynamic().xs = responsive(12)
                    asDynamic().sm = responsive(6)
                    asDynamic().md = responsive(4)
                    Feature {
                        icon = Shield.create {
                            size = 32
                        }
                        title = "KSP Multiplatform Support"
                        description = "Efficiently generate mapping code with Kotlin Symbol Processing across all supported platforms."
                    }
                }
                Grid {
                    item = true
                    asDynamic().xs = responsive(12)
                    asDynamic().sm = responsive(6)
                    asDynamic().md = responsive(4)
                    Feature {
                        icon = SquareTerminal.create {
                            size = 32
                        }
                        title = "Flexible Mapping"
                        description = "Maps both constructor parameters and public properties with setters for maximum flexibility."
                    }
                }
                Grid {
                    item = true
                    asDynamic().xs = responsive(12)
                    asDynamic().sm = responsive(6)
                    asDynamic().md = responsive(4)
                    Feature {
                        icon = Database.create {
                            size = 32
                        }
                        title = "Type Casting Support"
                        description = "Automatically handles type conversions between compatible property types."
                    }
                }
                Grid {
                    item = true
                    asDynamic().xs = responsive(12)
                    asDynamic().sm = responsive(6)
                    asDynamic().md = responsive(4)
                    Feature {
                        icon = Layers.create {
                            size = 32
                        }
                        title = "Java Compatibility"
                        description = "Full support for Java objects with get* functions for seamless integration."
                    }
                }
                Grid {
                    item = true
                    asDynamic().xs = responsive(12)
                    asDynamic().sm = responsive(6)
                    asDynamic().md = responsive(4)
                    Feature {
                        icon = Zap.create {
                            size = 32
                        }
                        title = "Multi-Source Support"
                        description = "Map from multiple source classes with separated configurations for complex scenarios."
                    }
                }
                Grid {
                    item = true
                    asDynamic().xs = responsive(12)
                    asDynamic().sm = responsive(6)
                    asDynamic().md = responsive(4)
                    Feature {
                        icon = SlidersVertical.create {
                            size = 32
                        }
                        title = "Customizable via Annotations"
                        description = "Powerful property annotations for controlling mapping behavior and handling edge cases."
                    }
                }
            }
        }
    }
}