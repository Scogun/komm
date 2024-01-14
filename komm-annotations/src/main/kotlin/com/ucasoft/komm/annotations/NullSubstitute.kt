package com.ucasoft.komm.annotations

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class NullSubstitute(val name: String = "", val default: MapDefault<*>)
