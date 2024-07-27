package com.ucasoft.komm.plugins

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType

interface KOMMCastPlugin: KOMMPlugin {

    fun forCast(sourceType: KSType, destinationType: KSType): Boolean

    fun cast(
        sourceProperty: KSDeclaration,
        sourceName: String,
        sourceType: KSType,
        destinationProperty: KSPropertyDeclaration,
        destinationType: KSType
    ): String
}