package com.ucasoft.komm.processor

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.ucasoft.komm.abstractions.KOMMConverter
import com.ucasoft.komm.annotations.*
import com.ucasoft.komm.processor.exceptions.KOMMException

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

    fun findSubstituteResolver(member: KSPropertyDeclaration): String? {
        val annotations = member.annotations.filter { it.shortName.asString() == NullSubstitute::class.simpleName }
            .associateWith(::associateWithFor)

        val annotation = filterAnnotationsByClass(forClass.toClassName(), annotations, member)

        if (annotation != null) {
            val resolverArgument =
                annotation.arguments.first { it.name?.asString() == NullSubstitute::default.name }.value as KSAnnotation
            return resolverArgument.arguments.first { it.name?.asString() == MapDefault<*>::resolver.name }.value.toString()
        }

        return null
    }

    private fun findMapAnnotation(
        forClass: ClassName,
        member: KSPropertyDeclaration,
        annotationName: String?,
        argumentName: String
    ): String? {
        val annotations =
            member.annotations.filter { it.shortName.asString() == annotationName }.associateWith(::associateWithFor)

        val annotation = filterAnnotationsByClass(forClass, annotations, member)

        if (annotation != null) {
            val resolverArgument = annotation.arguments.first { it.name?.asString() == argumentName }
            return resolverArgument.value.toString()
        }

        return null
    }

    fun getSuitedNamedAnnotations(member: KSPropertyDeclaration) =
        getSuitedNamedAnnotationsForClass(member).keys.toList()

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
        return getKOMMConverterAssociations(converterDeclaration).map { it.toClassName() }
    }

    private fun getKOMMConverterAssociations(
        declaration: KSClassDeclaration,
        typeSubstitutions: Map<String, KSType> = emptyMap()
    ): List<KSType> {
        for (superTypeReference in declaration.superTypes) {
            val superType = superTypeReference.resolve()
            val superDeclaration = superType.declaration as? KSClassDeclaration ?: continue

            if (superDeclaration.qualifiedName?.asString() == KOMMConverter::class.qualifiedName) {
                return listOfNotNull(
                    resolveTypeArgument(superType.arguments.getOrNull(0), typeSubstitutions),
                    resolveTypeArgument(superType.arguments.getOrNull(2), typeSubstitutions)
                )
            }

            val superSubstitutions = superDeclaration.typeParameters
                .zip(superType.arguments)
                .mapNotNull { (parameter, argument) ->
                    resolveTypeArgument(argument, typeSubstitutions)?.let { parameter.name.asString() to it }
                }
                .toMap()
            val associations = getKOMMConverterAssociations(superDeclaration, superSubstitutions)
            if (associations.isNotEmpty()) {
                return associations
            }
        }

        return emptyList()
    }

    private fun resolveTypeArgument(argument: KSTypeArgument?, typeSubstitutions: Map<String, KSType>): KSType? {
        val type = argument?.type?.resolve() ?: return null
        val typeParameter = type.declaration as? KSTypeParameter ?: return type
        return typeSubstitutions[typeParameter.name.asString()]
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
