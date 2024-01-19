package com.ucasoft.komm.annotations

import com.ucasoft.komm.abstractions.KOMMConverter
import com.ucasoft.komm.abstractions.KOMMResolver
import kotlin.reflect.KClass

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class MapFrom(val name: String, val from: Array<KClass<*>> = [])

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class MapConvert<C : KOMMConverter<*,*,*>>(val converter: KClass<C>, val name: String = "", val from: Array<KClass<*>> = [])

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class MapDefault<R: KOMMResolver<*, *>>(val resolver: KClass<R>, val from: Array<KClass<*>> = [])
