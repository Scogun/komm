package com.ucasoft.komm.annotations

import com.ucasoft.komm.abstractions.KOMMConverter
import com.ucasoft.komm.abstractions.KOMMResolver
import kotlin.reflect.KClass

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class MapFrom(val name: String)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class MapConvert<C : KOMMConverter<*,*,*>>(val name: String = "", val converter: KClass<C>)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class MapDefault<R: KOMMResolver<*, *>>(val resolver: KClass<R>)
