package com.ucasoft.komm.website

import com.ucasoft.wrappers.`react-syntax-highlighter`.SyntaxHighlighter
import mui.material.Box
import mui.material.Paper
import mui.material.Typography
import mui.system.sx
import react.FC
import react.Props
import web.cssom.BoxShadow
import web.cssom.Color
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.Margin
import web.cssom.Overflow
import web.cssom.Padding
import web.cssom.integer
import web.cssom.px
import web.cssom.rem
import web.cssom.rgb

val Code = FC<CodeProps> {
    Paper {
        sx {
            overflow = Overflow.hidden
            margin = Margin(4.px, 0.px)
            boxShadow = BoxShadow(3.px, 3.px, Color.currentcolor)
        }
        Box {
            sx {
                backgroundColor = rgb(0, 0, 0, 0.7)
                padding = Padding(1.px, 2.px)
                display = Display.flex
                justifyContent = JustifyContent.spaceBetween
            }
            Typography {
                sx {
                    color = rgb(255, 255, 255, 0.7)
                    fontSize = 0.9.rem
                    fontWeight = integer(500)
                }
                +it.title
            }
        }
        Box {
            sx {
                overflow = "auto".unsafeCast<Overflow>()
            }
            SyntaxHighlighter {
                language = "kotlin"
                +it.code
            }
        }
    }
}

external interface CodeProps : Props {
    var title: String
    var code: String
}