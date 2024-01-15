package com.ucasoft.komm.processor.extensions

import com.google.devtools.ksp.symbol.KSAnnotation

internal inline fun <reified T> KSAnnotation.getConfigValue(key: String) : T {
    val stringValue = this.arguments.first { it.name?.asString() == key }.value.toString()
    return when(T::class) {
        Boolean::class -> stringValue.toBoolean()
        else -> throw Exception()
    } as T
}