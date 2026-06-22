package com.ucasoft.komm.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class MapTargetDefault(
    val name: String,
    val default: MapDefault<*>,
    val `for`: Array<KClass<*>> = []
)
