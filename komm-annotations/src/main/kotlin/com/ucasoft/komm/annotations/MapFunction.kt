package com.ucasoft.komm.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class MapFunction(
    val packageName: String,
    val name: String = "",
    val `for`: Array<KClass<*>> = []
)
