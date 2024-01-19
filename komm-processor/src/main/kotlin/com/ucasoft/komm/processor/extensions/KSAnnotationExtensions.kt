package com.ucasoft.komm.processor.extensions

import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.reflect.KClass

internal inline fun <reified T: Any> KSAnnotation.getConfigValue(key: String) : T {
    val value = this.arguments.first { it.name?.asString() == key }.value.toString()
    return castConfigValue(value, T::class) as T
}

internal inline fun <reified T : Any> castConfigValue(value: String, `class`: KClass<T>) = when(`class`) {
    Boolean::class -> value.toBoolean()
    else -> throw Exception()
}