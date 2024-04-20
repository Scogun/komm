package com.ucasoft.komm.plugins.iterable

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.ucasoft.komm.annotations.NullSubstitute
import com.ucasoft.komm.plugins.KOMMCastPlugin

class IterablePlugin: BaseIterablePlugin(), KOMMCastPlugin {

    override fun forCast(sourceType: KSType, destinationType: KSType) =
        sourceType.isIterable() && destinationType.isIterable()

    override fun cast(
        sourceName: String,
        sourceType: KSType,
        destinationProperty: KSPropertyDeclaration,
        destinationType: KSType
    ): String {
        val destinationParam = destinationType.arguments.first()
        val sourceParam = sourceType.arguments.first()
        val stringBuilder = StringBuilder(sourceName)
        var fromCastDeclaration = sourceType.toClassName()
        val sourceIsNullable = sourceType.toTypeName().isNullable
        val destinationIsNullable = destinationType.toTypeName().isNullable
        val destinationHasNullSubstitute = destinationProperty.annotations.any { it.shortName.asString() == NullSubstitute::class.simpleName }
        val destinationIsNullOrNullSubstitute = destinationIsNullable || destinationHasNullSubstitute
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

    private fun ClassName.isAssignableFrom(other: ClassName) =
        this == other || other.simpleName.endsWith(this.simpleName)
}