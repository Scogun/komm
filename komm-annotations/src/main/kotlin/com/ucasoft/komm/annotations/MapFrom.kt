package com.ucasoft.komm.annotations

import kotlin.reflect.KClass

@Deprecated("Use MapName instead", ReplaceWith("MapName"))
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class MapFrom(val name: String, val from: Array<KClass<*>> = [])