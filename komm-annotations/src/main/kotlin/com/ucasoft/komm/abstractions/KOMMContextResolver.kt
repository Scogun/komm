package com.ucasoft.komm.abstractions

abstract class KOMMContextResolver<C, D, DM>(
    destination: D?,
    protected val context: C
): KOMMResolver<D, DM>(destination)