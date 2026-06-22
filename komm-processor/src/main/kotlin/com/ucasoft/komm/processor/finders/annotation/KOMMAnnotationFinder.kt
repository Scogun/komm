package com.ucasoft.komm.processor.finders.annotation

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.ucasoft.komm.abstractions.KOMMContextConverter
import com.ucasoft.komm.abstractions.KOMMConverter
import com.ucasoft.komm.annotations.*
import com.ucasoft.komm.processor.exceptions.KOMMException
import com.ucasoft.komm.processor.extensions.resolveTypeArgument

abstract class KOMMAnnotationFinder(forClass: KSType) {

    protected val forClassName: ClassName = forClass.toClassName()

    protected fun filterAnnotationsByClass(
        annotationMap: Map<KSAnnotation, List<ClassName>>,
        memberName: String
    ): KSAnnotation? {
        var annotations = annotationMap.filter { it.value.contains(forClassName) }
        if (annotations.isEmpty()) {
            annotations = annotationMap.filter { it.value.isEmpty() }
        }
        if (annotations.count() > 1) {
            val annotation = annotations.keys.first()
            throw KOMMException("There are too many @${annotation.shortName.asString()} annotations for $memberName property could be applied for ${forClassName.simpleName}")
        }

        return annotations.keys.firstOrNull()
    }

    protected fun associateWithFor(item: KSAnnotation): List<ClassName> {
        val forArgument = item.arguments.firstOrNull { it.name?.asString() == MapName::`for`.name }
        if (forArgument != null) {
            return (forArgument.value as ArrayList<*>).filterIsInstance<KSType>().map { it.toClassName() }
        }

        if (item.annotationType.toString() == MapConvert::class.simpleName) {
            return item.annotationType.element!!.typeArguments.take(2).mapNotNull { it.type?.resolve()?.toClassName() }
                .ifEmpty { getMapConvertAssociationsFromConverter(item) }
        }

        return emptyList()
    }

    protected fun getMapDefaultResolver(item: KSAnnotation): KSType? =
        item.arguments
            .first { it.name?.asString() == MapDefault<*>::resolver.name }
            .value as? KSType

    /*
    Workaround for KSP issue - https://github.com/google/ksp/issues/2622
     */
    private fun getMapConvertAssociationsFromConverter(item: KSAnnotation): List<ClassName> {
        val converterArgument = item.arguments
            .firstOrNull { it.name?.asString() == MapConvert<*, *, *>::converter.name }
            ?.value as? KSType
            ?: return emptyList()

        val converterDeclaration = converterArgument.declaration as? KSClassDeclaration ?: return emptyList()
        return getKOMMAssociations(converterDeclaration, KOMMConverter::class.qualifiedName!!, 0, 2)
            .ifEmpty { getKOMMAssociations(converterDeclaration, KOMMContextConverter::class.qualifiedName!!, 0, 3) }
            .map { it.toClassName() }
    }

    private fun getKOMMAssociations(
        declaration: KSClassDeclaration,
        superClassName: String,
        sourceIndex: Int,
        destinationIndex: Int,
        typeSubstitutions: Map<String, KSType> = emptyMap()
    ): List<KSType> {
        for (superTypeReference in declaration.superTypes) {
            val superType = superTypeReference.resolve()
            val superDeclaration = superType.declaration as? KSClassDeclaration ?: continue

            if (superDeclaration.qualifiedName?.asString() == superClassName) {
                return listOfNotNull(
                    superType.arguments.getOrNull(sourceIndex).resolveTypeArgument(typeSubstitutions),
                    superType.arguments.getOrNull(destinationIndex).resolveTypeArgument(typeSubstitutions)
                )
            }

            val superSubstitutions = superDeclaration.typeParameters
                .zip(superType.arguments)
                .mapNotNull { (parameter, argument) ->
                    argument.resolveTypeArgument(typeSubstitutions)?.let { parameter.name.asString() to it }
                }
                .toMap()
            val associations = getKOMMAssociations(
                superDeclaration,
                superClassName,
                sourceIndex,
                destinationIndex,
                superSubstitutions
            )
            if (associations.isNotEmpty()) {
                return associations
            }
        }

        return emptyList()
    }
}