package com.ucasoft.komm.plugins.iterable

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ITERABLE
import com.squareup.kotlinpoet.ksp.toClassName

abstract class BaseIterablePlugin {

    protected fun KSType.isIterable() = (this.declaration as KSClassDeclaration).getAllSuperTypes().any { it.toClassName() == ITERABLE }

    protected fun addSafeNullCall(add: Boolean, safe: String = "?", otherwise: String = "") = if (add) safe else otherwise

    protected fun safeCallOrNullAssertion(safe: Boolean) = if (safe) "?" else "!!"
}