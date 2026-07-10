package com.ucasoft.komm.plugins.iterable

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.STRING
import com.ucasoft.komm.plugins.iterable.annotations.KOMMIterableString

class IterableStringPlugin: BaseIterablePlugin() {

    override fun forCast(sourceType: KSType, destinationType: KSType) =
        (sourceType.isIterable() && destinationType.declaration.qualifiedName?.asString() == STRING.canonicalName) ||
        (sourceType.declaration.qualifiedName?.asString() == STRING.canonicalName && destinationType.isIterable())

    override fun cast(
        sourceProperty: KSDeclaration,
        sourceName: String,
        sourceType: KSType,
        destinationProperty: KSPropertyDeclaration,
        destinationType: KSType
    ): String {
        val (sourceIsNullable, destinationIsNullOrNullSubstitute) = parseMappingData(sourceType, sourceProperty, destinationType, destinationProperty)
        val stringBuilder = StringBuilder(sourceName)
        stringBuilder.append(addSafeNullCall(sourceIsNullable, safeCallOrNullAssertion(destinationIsNullOrNullSubstitute)))
        val delimiter = getDelimiter(sourceProperty, sourceType, destinationProperty, destinationType)
        if (sourceType.isIterable()) {
            stringBuilder.append(".joinToString(")
            if (delimiter != DEFAULT_DELIMITER) {
                stringBuilder.append("\"$delimiter\"")
            }

            stringBuilder.append(")")
        } else {
            stringBuilder.append(".split(\"$delimiter\")")
            if (!destinationType.declaration.qualifiedName!!.isAssignableFrom(LIST)) {
                stringBuilder.append("${addSafeNullCall(sourceIsNullable && destinationIsNullOrNullSubstitute)}.to${destinationType.declaration.qualifiedName!!.getShortName()}()")
            }
        }

        return stringBuilder.toString().trimEnd('?')
    }

    private fun getDelimiter(
        sourceProperty: KSDeclaration,
        sourceType: KSType,
        destinationProperty: KSPropertyDeclaration,
        destinationType: KSType
    ): String {
        val sourceAnnotation = sourceProperty.findIterableStringAnnotation()
        val destinationAnnotation = destinationProperty.findIterableStringAnnotation()
        val annotation = if (sourceType.isString() && destinationType.isIterable()) {
            sourceAnnotation ?: destinationAnnotation
        } else {
            destinationAnnotation ?: sourceAnnotation
        }

        return annotation
            ?.arguments
            ?.firstOrNull { it.name?.asString() == KOMMIterableString::delimiter.name }
            ?.value
            ?.toString()
            ?: DEFAULT_DELIMITER
    }

    private fun KSDeclaration.findIterableStringAnnotation() =
        annotations.firstOrNull { it.shortName.asString() == KOMMIterableString::class.simpleName }

    private fun KSType.isString() =
        declaration.qualifiedName?.asString() == STRING.canonicalName

    companion object {
        private const val DEFAULT_DELIMITER = ", "
    }
}
