package com.ucasoft.komm.plugins.iterable.annotations

@Target(AnnotationTarget.PROPERTY)
annotation class KOMMIterableString(val delimiter: String = ", ")
