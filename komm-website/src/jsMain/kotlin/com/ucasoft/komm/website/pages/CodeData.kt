package com.ucasoft.komm.website.pages

class CodeData(val type: Type, val code: String)

enum class Type {
    JVM,
    KMP
}