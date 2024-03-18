package com.ucasoft.komm.processor.extensions

import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.reflect.KClass

internal inline fun <reified T: Any> KSAnnotation.getConfigValue(key: String) : T {
    val value = this.arguments.first { it.name?.asString() == key }.value.toString()
    val castValue = castConfigValue(value, T::class)
    require(castValue is T) { "Value $value cannot be cast to ${T::class}" }
    return castValue
}

internal inline fun <reified T : Any> castConfigValue(value: String, `class`: KClass<T>) = when(`class`) {
    String::class -> value
    Boolean::class -> value.toBoolean()
    else -> throw Exception()
}