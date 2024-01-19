package com.ucasoft.komm.annotations

import com.ucasoft.komm.abstractions.KOMMConverter
import kotlin.reflect.KClass

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class MapConvert<S: Any, C : KOMMConverter<S, *, *>>(val converter: KClass<C>, val name: String = "")