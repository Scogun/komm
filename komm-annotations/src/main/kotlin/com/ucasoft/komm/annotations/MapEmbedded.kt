package com.ucasoft.komm.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class MapEmbedded(val name: String, val `for`: Array<KClass<*>> = [])
