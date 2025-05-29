package com.ucasoft.komm.website.pages

import com.ucasoft.wrappers.`react-syntax-highlighter`.SyntaxHighlighter
import mui.material.Box
import mui.material.Tab
import mui.material.Tabs
import react.FC
import react.Props
import react.ReactNode

external interface CodeTabsProps : Props {
    var type: Type?
    var items: List<CodeData>
    var typeChange: (Type) -> Unit
}

val CodeTabs = FC<CodeTabsProps> { tabs ->

    Tabs {
        value = tabs.type
        onChange = { _, newType ->
            tabs.typeChange(newType as Type)
        }
        tabs.items.map {
            Tab {
                value = it.type
                label = ReactNode(it.type.toString())
            }
        }
    }
    tabs.items.map {
        Box {
            hidden = tabs.type != it.type
            SyntaxHighlighter {
                language = "kotlin"
                +it.code
            }
        }
    }
}