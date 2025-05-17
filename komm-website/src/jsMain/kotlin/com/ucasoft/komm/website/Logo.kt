package com.ucasoft.komm.website

import mui.material.Stack
import mui.material.StackDirection.Companion.row
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.FC
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.dom.svg.ReactSVG.path
import react.dom.svg.ReactSVG.svg
import web.cssom.AlignItems
import web.cssom.Color
import web.cssom.integer
import web.cssom.number

val Logo = FC {
    Stack {
        direction = responsive(row)
        spacing = responsive(1)
        asDynamic().alignItems = AlignItems.center
        svg {
            width = 40.0
            height = 40.0
            viewBox = "0 0 80 80"
            fill = "none"
            xmlns = "http://www.w3.org/2000/svg"
            path {
                d = "M40 0L0 40L40 80L80 40L40 0Z"
                fill = "#7F52FF"
            }
            path {
                d = "M40 20L20 40L40 60L60 40L40 20Z"
                fill = "white"
            }
        }
        Typography {
            variant = TypographyVariant.h6
            component = div
            sx {
                flexGrow = number(1.0)
                fontWeight = integer(800)
                color = Color("primary.main")
            }
            span {
               +"KOMM"
            }
        }
    }
}