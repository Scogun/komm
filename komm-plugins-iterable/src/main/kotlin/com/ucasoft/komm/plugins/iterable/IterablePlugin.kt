package com.ucasoft.komm.plugins.iterable

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ksp.toClassName

class IterablePlugin: BaseIterablePlugin() {

    override fun forCast(sourceType: KSType, destinationType: KSType) =
        sourceType.isIterable() && destinationType.isIterable()

    override fun cast(
        sourceName: String,
        sourceType: KSType,
        destinationProperty: KSPropertyDeclaration,
        destinationType: KSType
    ): String {
        TODO("Not yet implemented")
    }

    override fun cast(
        sourceProperty: KSDeclaration,
        sourceName: String,
        sourceType: KSType,
        destinationProperty: KSPropertyDeclaration,
        destinationType: KSType
    ): String {
        val destinationParam = destinationType.arguments.first()
        val sourceParam = sourceType.arguments.first()
        val stringBuilder = StringBuilder(sourceName)
        var fromCastDeclaration = sourceType.toClassName()
        val (sourceIsNullable, destinationIsNullOrNullSubstitute) = parseMappingData(sourceType, sourceProperty, destinationType, destinationProperty)
        stringBuilder.append(addSafeNullCall(sourceIsNullable, safeCallOrNullAssertion(destinationIsNullOrNullSubstitute)))
        if (!destinationParam.type!!.resolve().isAssignableFrom(sourceParam.type!!.resolve())) {
            stringBuilder.append(".map{ it.to${destinationParam.type}() }")
            fromCastDeclaration = LIST
        }
        if (!destinationType.toClassName().isAssignableFrom(fromCastDeclaration)) {
            stringBuilder.append("${addSafeNullCall(sourceIsNullable && destinationIsNullOrNullSubstitute)}.to${destinationType.toClassName().simpleName}()")
        }
        return stringBuilder.toString().trimEnd('?')
    }
}