package com.ucasoft.komm.plugins.iterable

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ITERABLE
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.ucasoft.komm.annotations.NullSubstitute
import com.ucasoft.komm.plugins.KOMMCastPlugin

abstract class BaseIterablePlugin: KOMMCastPlugin {

    protected fun KSType.isIterable() = (this.declaration as KSClassDeclaration).getAllSuperTypes().any { it.toClassName() == ITERABLE }

    protected fun addSafeNullCall(add: Boolean, safe: String = "?", otherwise: String = "") = if (add) safe else otherwise

    protected fun safeCallOrNullAssertion(safe: Boolean) = if (safe) "?" else "!!"

    protected fun parseMappingData(sourceType: KSType, destinationType: KSType, destinationProperty: KSPropertyDeclaration): Pair<Boolean, Boolean>{
        val sourceIsNullable = sourceType.toTypeName().isNullable
        val destinationHasNullSubstitute = destinationProperty.annotations.any { it.shortName.asString() == NullSubstitute::class.simpleName }
        val destinationIsNullOrNullSubstitute = destinationType.toTypeName().isNullable || destinationHasNullSubstitute
        return Pair(sourceIsNullable, destinationIsNullOrNullSubstitute)
    }
}