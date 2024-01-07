package com.ucasoft.komm.abstractions

abstract class KOMMResolver<D, DM>(protected val destination: D?) {

    abstract fun resolve() : DM
}