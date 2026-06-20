package com.ucasoft.komm.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class KOMMMap(
    val from: Array<KClass<*>> = [],
    val to: Array<KClass<*>> = [],
    val context: KClass<*> = Unit::class,
    val config: MapConfiguration = MapConfiguration()
)
