package com.ucasoft.komm.processor.finders.annotation

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.ucasoft.komm.annotations.MapConvert
import com.ucasoft.komm.annotations.MapDefault
import com.ucasoft.komm.annotations.MapFunction
import com.ucasoft.komm.annotations.MapName
import com.ucasoft.komm.annotations.NullSubstitute

class KOMMPropertyAnnotationFinder(forClass: KSType) : KOMMAnnotationFinder(forClass) {

    private val namedAnnotations =
        listOf(
            MapName::class.simpleName,
            MapConvert::class.simpleName,
            NullSubstitute::class.simpleName
        )

    fun findResolver(member: KSPropertyDeclaration) = findMapAnnotation(
        member,
        MapDefault::class.simpleName,
        MapDefault<*>::resolver.name
    )

    fun findConverter(member: KSPropertyDeclaration) =
        findMapAnnotation(
            member,
            MapConvert::class.simpleName,
            MapConvert<*, *, *>::converter.name
        )

    fun findFunction(member: KSPropertyDeclaration): Pair<String, String>? {
        val annotations = member.annotations.filter { it.shortName.asString() == MapFunction::class.simpleName }
            .associateWith(::associateWithFor)

        val annotation = filterAnnotationsByClass(annotations, member.simpleName.asString())
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

        val annotation = filterAnnotationsByClass(annotations, member.simpleName.asString()) ?: return null
        val defaultArgument =
            annotation.arguments.first { it.name?.asString() == NullSubstitute::default.name }.value as KSAnnotation
        return getMapDefaultResolver(defaultArgument)
    }

    fun getSuitedNamedAnnotations(member: KSPropertyDeclaration) =
        getSuitedNamedAnnotationsForClass(member)
            .filter { it.value.isEmpty() || it.value.contains(forClassName) }
            .keys
            .toList()

    fun getSuitedNamedAnnotation(member: KSPropertyDeclaration) =
        filterAnnotationsByClass(getSuitedNamedAnnotationsForClass(member), member.simpleName.asString())

    private fun findMapAnnotation(
        member: KSPropertyDeclaration,
        annotationName: String?,
        argumentName: String
    ): KSType? {
        val annotation = findAnnotation(member, annotationName)

        if (annotation != null) {
            val resolverArgument = annotation.arguments.first { it.name?.asString() == argumentName }
            return resolverArgument.value as? KSType
        }

        return null
    }

    private fun findAnnotation(
        member: KSPropertyDeclaration,
        annotationName: String?
    ): KSAnnotation? {
        val annotations =
            member.annotations.filter { it.shortName.asString() == annotationName }.associateWith(::associateWithFor)

        return filterAnnotationsByClass(annotations, member.simpleName.asString())
    }

    private fun getSuitedNamedAnnotationsForClass(member: KSPropertyDeclaration) =
        member.annotations.filter { it.shortName.asString() in namedAnnotations }
            .associateWith(::associateWithFor)
}