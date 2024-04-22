package com.ucasoft.komm.plugins.iterable

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.ksp.toClassName

class IterableStringPlugin: BaseIterablePlugin() {

    override fun forCast(sourceType: KSType, destinationType: KSType) =
        sourceType.isIterable() && destinationType.toClassName() == STRING

    override fun cast(
        sourceName: String,
        sourceType: KSType,
        destinationProperty: KSPropertyDeclaration,
        destinationType: KSType
    ): String {
        val (sourceIsNullable, destinationIsNullOrNullSubstitute) = parseMappingData(sourceType, destinationType, destinationProperty)
        val stringBuilder = StringBuilder(sourceName)
        stringBuilder.append(addSafeNullCall(sourceIsNullable, safeCallOrNullAssertion(destinationIsNullOrNullSubstitute)))
        stringBuilder.append(".joinToString()")
        return stringBuilder.toString().trimEnd('?')
    }
}