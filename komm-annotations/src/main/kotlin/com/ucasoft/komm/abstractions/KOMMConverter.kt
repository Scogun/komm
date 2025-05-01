package com.ucasoft.komm.abstractions

import kotlin.reflect.KClass

abstract class KOMMConverter<S, SM, D, DM>(protected val source: S) {

    abstract fun convert(sourceMember: SM) : DM
}