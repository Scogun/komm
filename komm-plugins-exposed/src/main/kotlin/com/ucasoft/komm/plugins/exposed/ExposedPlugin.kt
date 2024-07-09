package com.ucasoft.komm.plugins.exposed

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.ucasoft.komm.plugins.KOMMTypePlugin
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

class ExposedPlugin: KOMMTypePlugin {

    override fun forType(sourceType: KSType) = (sourceType.declaration as KSClassDeclaration).superTypes.any { it.toTypeName() == Table::class.asTypeName() }

    override fun sourceType(sourceType: KSType) = ResultRow::class

    override fun forCast(sourceType: KSType, destinationType: KSType) = sourceType.toClassName() == Column::class.asClassName()

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
        val result = StringBuilder("this[")
        val sourceObject = sourceProperty.parentDeclaration!!.simpleName.asString()
        result.append("$sourceObject.$sourceName]")
        if (destinationType.toTypeName() != sourceType.arguments.first().toTypeName()) {
            result.append(".to${destinationProperty.type}()")
        }
        return result.toString()
    }
}