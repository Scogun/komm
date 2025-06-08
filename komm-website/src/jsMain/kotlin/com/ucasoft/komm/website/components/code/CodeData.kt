package com.ucasoft.komm.website.components.code

class CodeData(val type: Type, val code: String)

enum class Type {
    JVM,
    KMP
}