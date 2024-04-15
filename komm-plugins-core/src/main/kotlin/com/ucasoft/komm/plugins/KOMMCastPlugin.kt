package com.ucasoft.komm.plugins

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType

abstract class KOMMCastPlugin: KOMMPlugin {

    abstract fun forCast(sourceType: KSType, destinationType: KSType): Boolean

    abstract fun cast(
        sourceName: String,
        sourceType: KSType,
        destinationProperty: KSPropertyDeclaration,
        destinationType: KSType
    ): String
}