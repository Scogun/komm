package com.ucasoft.komm.abstractions

abstract class KOMMContextConverter<S, SM, C, D, DM>(
    source: S,
    protected val context: C
) : KOMMConverter<S, SM, D, DM>(source)
