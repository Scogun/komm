package com.ucasoft.komm.processor

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.ucasoft.komm.annotations.*
import com.ucasoft.komm.processor.exceptions.KOMMException

class KOMMAnnotationFinder(private val source: KSType) {

    private val namedAnnotations =
        listOf(MapFrom::class.simpleName, MapName::class.simpleName, MapConvert::class.simpleName, NullSubstitute::class.simpleName)

    fun findResolver(member: KSPropertyDeclaration) = findMapAnnotation(
        source.toClassName(),
        member,
        MapDefault::class.simpleName,
        MapDefault<*>::resolver.name
    )

    fun findConverter(member: KSPropertyDeclaration) =
        findMapAnnotation(
            source.toClassName(),
            member,
            MapConvert::class.simpleName,
            MapConvert<*, *>::converter.name
        )

    fun findSubstituteResolver(member: KSPropertyDeclaration): String? {
        val annotations = member.annotations.filter { it.shortName.asString() == NullSubstitute::class.simpleName }
            .associateWith(::associateWithFrom)

        val annotation = filterAnnotationsBySource(source.toClassName(), annotations, member)

        if (annotation != null) {
            val resolverArgument =
                annotation.arguments.first { it.name?.asString() == NullSubstitute::default.name }.value as KSAnnotation
            return resolverArgument.arguments.first { it.name?.asString() == MapDefault<*>::resolver.name }.value.toString()
        }

        return null
    }

    private fun findMapAnnotation(
        source: ClassName,
        member: KSPropertyDeclaration,
        annotationName: String?,
        argumentName: String
    ): String? {
        val annotations =
            member.annotations.filter { it.shortName.asString() == annotationName }.associateWith(::associateWithFrom)

        val annotation = filterAnnotationsBySource(source, annotations, member)

        if (annotation != null) {
            val resolverArgument = annotation.arguments.first { it.name?.asString() == argumentName }
            return resolverArgument.value.toString()
        }

        return null
    }

    fun getSuitedNamedAnnotation(member: KSPropertyDeclaration) : KSAnnotation? {
        val mapFroms = member.annotations.filter { it.shortName.asString() in namedAnnotations }
            .associateWith(::associateWithFrom)

        return filterAnnotationsBySource(source.toClassName(), mapFroms, member)
    }

    private fun associateWithFrom(item: KSAnnotation): List<ClassName> {
        val fromArgument = item.arguments.firstOrNull { it.name?.asString() == MapName::from.name }
        if (fromArgument != null) {
            return (fromArgument.value as ArrayList<*>).filterIsInstance<KSType>().map { it.toClassName() }
        }

        if (item.annotationType.toString() == MapConvert::class.simpleName) {
            return listOf(item.annotationType.element!!.typeArguments.first().type!!.resolve().toClassName())
        }

        return emptyList()
    }

    private fun filterAnnotationsBySource(
        source: ClassName,
        annotationMap: Map<KSAnnotation, List<ClassName>>,
        member: KSPropertyDeclaration
    ): KSAnnotation? {
        var annotations = annotationMap.filter { it.value.contains(source) }
        if (annotations.isEmpty()) {
            annotations = annotationMap.filter { it.value.isEmpty() }
        }
        if (annotations.count() > 1) {
            val annotation = annotations.keys.first()
            throw KOMMException("There are too many @${annotation.shortName.asString()} annotations for ${member.simpleName.asString()} property could be applied for ${source.simpleName}")
        }

        return annotations.keys.firstOrNull()
    }
}