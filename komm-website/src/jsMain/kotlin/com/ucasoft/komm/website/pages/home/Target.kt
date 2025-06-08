package com.ucasoft.komm.website.pages.home

import mui.material.Box
import mui.material.Paper
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import web.cssom.Color
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.Transition
import web.cssom.px

val Target = FC<TargetProps> {
    Paper {
        elevation = 3
        sx {
            padding = 2.px
            backgroundColor = Color("primary.main")
            hover { backgroundColor = Color("primary.light") }
            transition = "background-color 0.3s".unsafeCast<Transition>()
        }
        Box {
            sx {
                display = Display.flex
                justifyContent = JustifyContent.center
                marginBottom = 1.px
                color = Color("white")
            }
            +it.icon
        }
        Typography {
            variant = TypographyVariant.subtitle1
            +it.title
        }
    }
}

external interface TargetProps: Props {
    var icon: ReactNode
    var title: String
    var description: String
}