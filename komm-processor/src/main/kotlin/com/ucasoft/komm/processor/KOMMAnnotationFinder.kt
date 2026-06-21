package com.ucasoft.komm.processor

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.ucasoft.komm.abstractions.KOMMContextConverter
import com.ucasoft.komm.abstractions.KOMMContextResolver
import com.ucasoft.komm.abstractions.KOMMConverter
import com.ucasoft.komm.annotations.*
import com.ucasoft.komm.processor.exceptions.KOMMException
import com.ucasoft.komm.processor.extensions.resolveTypeArgument

class KOMMAnnotationFinder(private val forClass: KSType) {

    private val namedAnnotations =
        listOf(
            MapName::class.simpleName,
            MapConvert::class.simpleName,
            NullSubstitute::class.simpleName
        )

    fun findResolver(member: KSPropertyDeclaration) = findMapAnnotation(
        forClass.toClassName(),
        member,
        MapDefault::class.simpleName,
        MapDefault<*>::resolver.name
    )

    fun findConverter(member: KSPropertyDeclaration) =
        findMapAnnotation(
            forClass.toClassName(),
            member,
            MapConvert::class.simpleName,
            MapConvert<*, *, *>::converter.name
        )

    fun findFunction(member: KSPropertyDeclaration): Pair<String, String>? {
        val annotations = member.annotations.filter { it.shortName.asString() == MapFunction::class.simpleName }
            .associateWith(::associateWithFor)

        val annotation = filterAnnotationsByClass(forClass.toClassName(), annotations, member)
            ?: return null

        val packageName = annotation.arguments
            .first { it.name?.asString() == MapFunction::packageName.name }
            .value
            .toString()
        val name = annotation.arguments
            .first { it.name?.asString() == MapFunction::name.name }
            .value
            .toString()

        return packageName to name
    }

    fun findSubstituteResolver(member: KSPropertyDeclaration): KSType? {
        val annotations = member.annotations.filter { it.shortName.asString() == NullSubstitute::class.simpleName }
            .associateWith(::associateWithFor)

        val annotation = filterAnnotationsByClass(forClass.toClassName(), annotations, member)

        if (annotation != null) {
            val resolverArgument =
                annotation.arguments.first { it.name?.asString() == NullSubstitute::default.name }.value as KSAnnotation
            return resolverArgument.arguments.first { it.name?.asString() == MapDefault<*>::resolver.name }.value as? KSType
        }

        return null
    }

    private fun findMapAnnotation(
        forClass: ClassName,
        member: KSPropertyDeclaration,
        annotationName: String?,
        argumentName: String
    ): KSType? {
        val annotation = findAnnotation(forClass, member, annotationName)

        if (annotation != null) {
            val resolverArgument = annotation.arguments.first { it.name?.asString() == argumentName }
            return resolverArgument.value as? KSType
        }

        return null
    }

    private fun findAnnotation(
        forClass: ClassName,
        member: KSPropertyDeclaration,
        annotationName: String?
    ): KSAnnotation? {
        val annotations =
            member.annotations.filter { it.shortName.asString() == annotationName }.associateWith(::associateWithFor)

        return filterAnnotationsByClass(forClass, annotations, member)
    }

    fun getSuitedNamedAnnotations(member: KSPropertyDeclaration) =
        getSuitedNamedAnnotationsForClass(member)
            .filter { it.value.isEmpty() || it.value.contains(forClass.toClassName()) }
            .keys
            .toList()

    fun getSuitedNamedAnnotation(member: KSPropertyDeclaration) =
        filterAnnotationsByClass(forClass.toClassName(), getSuitedNamedAnnotationsForClass(member), member)

    fun getSuitedEmbeddedAnnotations(classDeclaration: KSClassDeclaration): List<KSAnnotation> =
        classDeclaration.annotations
            .filter { it.shortName.asString() == MapEmbedded::class.simpleName }
            .associateWith(::associateWithFor)
            .filter { it.value.isEmpty() || it.value.contains(forClass.toClassName()) }
            .keys
            .toList()

    private fun getSuitedNamedAnnotationsForClass(member: KSPropertyDeclaration) =
        member.annotations.filter { it.shortName.asString() in namedAnnotations }
            .associateWith(::associateWithFor)

    private fun associateWithFor(item: KSAnnotation): List<ClassName> {
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

    private fun filterAnnotationsByClass(
        forClass: ClassName,
        annotationMap: Map<KSAnnotation, List<ClassName>>,
        member: KSPropertyDeclaration
    ): KSAnnotation? {
        var annotations = annotationMap.filter { it.value.contains(forClass) }
        if (annotations.isEmpty()) {
            annotations = annotationMap.filter { it.value.isEmpty() }
        }
        if (annotations.count() > 1) {
            val annotation = annotations.keys.first()
            throw KOMMException("There are too many @${annotation.shortName.asString()} annotations for ${member.simpleName.asString()} property could be applied for ${forClass.simpleName}")
        }

        return annotations.keys.firstOrNull()
    }
}
