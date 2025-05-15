package com.ucasoft.komm.website

import com.ucasoft.wrappers.`react-syntax-highlighter`.SyntaxHighlighter
import react.FC

val Code = FC {
    SyntaxHighlighter {
        language = "kotlin"
        +"""
data class UserDto(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String?
)    
""".trimIndent()
    }
}