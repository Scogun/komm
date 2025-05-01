package com.ucasoft.komm.abstractions

abstract class KOMMConverter<S, SM, D, DM>(protected val source: S) {

    abstract fun convert(sourceMember: SM) : DM
}