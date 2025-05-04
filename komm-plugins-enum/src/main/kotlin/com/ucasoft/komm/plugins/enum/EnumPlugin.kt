package com.ucasoft.komm.plugins.enum

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.ucasoft.komm.annotations.NullSubstitute
import com.ucasoft.komm.plugins.KOMMCastPlugin
import com.ucasoft.komm.plugins.enum.annotations.KOMMEnum

class EnumPlugin : KOMMCastPlugin {

    override fun forCast(sourceType: KSType, destinationType: KSType) =
        sourceType.isEnum() && destinationType.isEnum() && !destinationType.isAssignableFrom(sourceType.makeNotNullable())

    override fun cast(
        sourceProperty: KSDeclaration,
        sourceName: String,
        sourceType: KSType,
        destinationProperty: KSPropertyDeclaration,
        destinationType: KSType
    ): String {
        val (sourceIsNullable, destinationIsNullOrNullSubstitute) = parseMappingData(
            sourceType,
            sourceProperty,
            destinationType,
            destinationProperty
        )
        val pluginAnnotation = listOfNotNull(
            destinationProperty.annotations.firstOrNull { it.shortName.asString() == KOMMEnum::class.simpleName },
            sourceProperty.annotations.firstOrNull { it.shortName.asString() == KOMMEnum::class.simpleName }
        ).firstOrNull()
        val stringBuilder = StringBuilder()
        if (sourceIsNullable && destinationIsNullOrNullSubstitute) {
            stringBuilder.append("(if ($sourceName != null) ")
        }
        val destinationTypeName = destinationType.toClassName().simpleNames.joinToString(".")
        stringBuilder.append(destinationTypeName)
        stringBuilder.append(".valueOf(")
        if (pluginAnnotation != null) {
            stringBuilder.append("if ($destinationTypeName.entries.any { it.name == $sourceName")
            if (sourceIsNullable && !destinationIsNullOrNullSubstitute) {
                stringBuilder.append("!!")
            }
            stringBuilder.append(".name }) ")
        }
        stringBuilder.append(sourceName)
        if (sourceIsNullable && !destinationIsNullOrNullSubstitute) {
            stringBuilder.append("!!")
        }
        stringBuilder.append(".name")
        if (pluginAnnotation != null) {
            val defaultArgument = pluginAnnotation.arguments.first { it.name?.asString() == KOMMEnum::default.name }
            stringBuilder.append(" else \"${defaultArgument.value}\"")
        }
        stringBuilder.append(")")
        if (sourceIsNullable && destinationIsNullOrNullSubstitute) {
            stringBuilder.append(" else null)")
        }
        return stringBuilder.toString()
    }

    private fun KSType.isEnum() = (this.declaration as KSClassDeclaration).classKind == ClassKind.ENUM_CLASS

    private fun parseMappingData(
        sourceType: KSType,
        sourceProperty: KSDeclaration,
        destinationType: KSType,
        destinationProperty: KSPropertyDeclaration
    ): Pair<Boolean, Boolean> {
        val sourceIsNullable = sourceType.toTypeName().isNullable
        val destinationHasNullSubstitute =
            destinationProperty.annotations.any { it.shortName.asString() == NullSubstitute::class.simpleName }
        val sourceHasNullSubstitute =
            sourceProperty.annotations.any { it.shortName.asString() == NullSubstitute::class.simpleName }
        val destinationIsNullOrNullSubstitute =
            destinationType.toTypeName().isNullable || destinationHasNullSubstitute || sourceHasNullSubstitute
        return Pair(sourceIsNullable, destinationIsNullOrNullSubstitute)
    }
}