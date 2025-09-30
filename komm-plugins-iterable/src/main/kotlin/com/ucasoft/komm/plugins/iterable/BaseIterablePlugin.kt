package com.ucasoft.komm.plugins.iterable

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ITERABLE
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.ucasoft.komm.annotations.NullSubstitute
import com.ucasoft.komm.plugins.KOMMCastPlugin

abstract class BaseIterablePlugin: KOMMCastPlugin {

    protected fun KSType.isIterable() = (this.declaration as KSClassDeclaration).getAllSuperTypes().any { it.declaration.qualifiedName?.asString() == ITERABLE.canonicalName }

    protected fun addSafeNullCall(add: Boolean, safe: String = "?", otherwise: String = "") = if (add) safe else otherwise

    protected fun safeCallOrNullAssertion(safe: Boolean) = if (safe) "?" else "!!"

    protected fun parseMappingData(sourceType: KSType, sourceProperty: KSDeclaration, destinationType: KSType, destinationProperty: KSPropertyDeclaration): Pair<Boolean, Boolean>{
        val sourceIsNullable = sourceType.toTypeName().isNullable
        val destinationHasNullSubstitute = destinationProperty.annotations.any { it.shortName.asString() == NullSubstitute::class.simpleName }
        val sourceHasNullSubstitute = sourceProperty.annotations.any { it.shortName.asString() == NullSubstitute::class.simpleName }
        val destinationIsNullOrNullSubstitute = destinationType.toTypeName().isNullable || destinationHasNullSubstitute || sourceHasNullSubstitute
        return Pair(sourceIsNullable, destinationIsNullOrNullSubstitute)
    }

    protected fun KSName.isAssignableFrom(other: ClassName) =
        other.simpleName.endsWith(this.getShortName())

    protected fun KSName.isAssignableFrom(other: String) =
        other.endsWith(this.getShortName())
}