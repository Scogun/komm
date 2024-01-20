package com.ucasoft.komm.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class MapFrom(val name: String, val from: Array<KClass<*>> = [])