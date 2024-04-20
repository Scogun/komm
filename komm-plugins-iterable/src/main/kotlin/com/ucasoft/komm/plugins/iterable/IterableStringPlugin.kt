package com.ucasoft.komm.plugins.iterable

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.ucasoft.komm.annotations.NullSubstitute
import com.ucasoft.komm.plugins.KOMMCastPlugin

class IterableStringPlugin: BaseIterablePlugin(), KOMMCastPlugin {

    override fun forCast(sourceType: KSType, destinationType: KSType) =
        sourceType.isIterable() && destinationType.toClassName() == STRING

    override fun cast(
        sourceName: String,
        sourceType: KSType,
        destinationProperty: KSPropertyDeclaration,
        destinationType: KSType
    ): String {
        val stringBuilder = StringBuilder(sourceName)
        val sourceIsNullable = sourceType.toTypeName().isNullable
        val destinationHasNullSubstitute = destinationProperty.annotations.any { it.shortName.asString() == NullSubstitute::class.simpleName }
        val destinationIsNullOrNullSubstitute = destinationType.toTypeName().isNullable || destinationHasNullSubstitute
        stringBuilder.append(addSafeNullCall(sourceIsNullable, safeCallOrNullAssertion(destinationIsNullOrNullSubstitute)))
        stringBuilder.append(".joinToString()")
        return stringBuilder.toString().trimEnd('?')
    }
}