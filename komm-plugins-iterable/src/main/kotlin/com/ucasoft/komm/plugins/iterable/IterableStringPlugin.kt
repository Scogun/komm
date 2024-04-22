package com.ucasoft.komm.plugins.iterable

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.ksp.toClassName
import com.ucasoft.komm.plugins.iterable.annotations.KOMMIterableString

class IterableStringPlugin: BaseIterablePlugin() {

    override fun forCast(sourceType: KSType, destinationType: KSType) =
        (sourceType.isIterable() && destinationType.toClassName() == STRING) ||
        (sourceType.toClassName() == STRING && destinationType.isIterable())

    override fun cast(
        sourceName: String,
        sourceType: KSType,
        destinationProperty: KSPropertyDeclaration,
        destinationType: KSType
    ): String {
        val (sourceIsNullable, destinationIsNullOrNullSubstitute) = parseMappingData(sourceType, destinationType, destinationProperty)
        val stringBuilder = StringBuilder(sourceName)
        stringBuilder.append(addSafeNullCall(sourceIsNullable, safeCallOrNullAssertion(destinationIsNullOrNullSubstitute)))
        val pluginAnnotation = destinationProperty.annotations.firstOrNull { it.shortName.asString() == KOMMIterableString::class.simpleName }
        val delimiter = pluginAnnotation?.arguments?.firstOrNull { it.name?.asString() == KOMMIterableString::delimiter.name }?.value?.toString() ?: DEFAULT_DELIMITER
        if (sourceType.isIterable()) {
            stringBuilder.append(".joinToString(")
            if (delimiter != DEFAULT_DELIMITER) {
                stringBuilder.append("\"$delimiter\"")
            }

            stringBuilder.append(")")
        } else {
            stringBuilder.append(".split(\"$delimiter\")")
            if (!destinationType.toClassName().isAssignableFrom(LIST)) {
                stringBuilder.append("${addSafeNullCall(sourceIsNullable && destinationIsNullOrNullSubstitute)}.to${destinationType.toClassName().simpleName}()")
            }
        }

        return stringBuilder.toString().trimEnd('?')
    }

    companion object {
        private const val DEFAULT_DELIMITER = ", "
    }
}