package com.ucasoft.komm.annotations

import com.ucasoft.komm.abstractions.KOMMResolver
import kotlin.reflect.KClass

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class MapDefault<R: KOMMResolver<*, *>>(val resolver: KClass<R>, val `for`: Array<KClass<*>> = [])