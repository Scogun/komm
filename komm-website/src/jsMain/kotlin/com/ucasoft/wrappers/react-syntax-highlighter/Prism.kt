@file:JsModule("react-syntax-highlighter")
@file:JsNonModule

package com.ucasoft.wrappers.`react-syntax-highlighter`

import react.FC
import react.PropsWithChildren

@JsName("Prism")
external val SyntaxHighlighter: FC<SyntaxHighlighterProps>

external interface SyntaxHighlighterProps : PropsWithChildren {
    var language: String
}