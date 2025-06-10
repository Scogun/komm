package com.ucasoft.komm.website.components.code

class CodeData(val type: Type, val code: String, val language: String = "kotlin")

enum class Type {
    JVM,
    KMP
}