package com.ucasoft.komm.processor

import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ksp.toTypeName
import com.ucasoft.komm.annotations.*
import com.ucasoft.komm.processor.exceptions.KOMMCastException
import com.ucasoft.komm.processor.exceptions.KOMMException
import com.ucasoft.komm.processor.extensions.getConfigValue

class KOMMPropertyMapper(source: KSType, private val config: KSAnnotation) {

    private val sourceProperties = getSourceProperties(source)

    private val annotationFinder = KOMMAnnotationFinder(source)

    fun map(destination: KSPropertyDeclaration, mapTo: KOMMVisitor.MapTo): String? {
        val resolver = annotationFinder.findResolver(destination)
        if (!config.getConfigValue<Boolean>(MapConfiguration::mapDefaultAsFallback.name) && resolver != null) {
            return "$destination = ${mapResolver(resolver, mapTo)}"
        }

        val sourceName = getSourceName(destination)
        if (!sourceProperties.containsKey(sourceName)) {
            return handleNoSourceProperty(resolver, destination, sourceName, mapTo)
        }

        val converter = annotationFinder.findConverter(destination)
        val nullSubstituteResolver = annotationFinder.findSubstituteResolver(destination)
        return if (converter != null) {
            "$destination = $converter(this).convert($sourceName)"
        } else if (nullSubstituteResolver != null) {
            "$destination = ${
                getSourceWithCast(destination, sourceProperties[sourceName], config).trimEnd('!').replace("!!", "?")
            } ?: ${mapResolver(nullSubstituteResolver, mapTo)}"
        } else {
            "$destination = ${getSourceWithCast(destination, sourceProperties[sourceName], config)}"
        }
    }

    private fun mapResolver(resolver: String, mapTo: KOMMVisitor.MapTo) =
        "$resolver(${if (mapTo == KOMMVisitor.MapTo.Constructor) "null" else "it"}).resolve()"

    private fun getSourceName(member: KSPropertyDeclaration): String {
        val mapFrom = annotationFinder.getSuitedNamedAnnotation(member)

        if (mapFrom != null) {
            val nameArgument = mapFrom.arguments.first { it.name?.asString() == MapFrom::name.name }
            val name = nameArgument.value.toString()
            if (name.isNotEmpty()) {
                return name
            }
        }

        return member.toString()
    }

    private fun handleNoSourceProperty(
        resolver: String?,
        destination: KSPropertyDeclaration,
        sourceName: String,
        mapTo: KOMMVisitor.MapTo
    ) = when {
        resolver != null -> "$destination = ${mapResolver(resolver, mapTo)}"
        mapTo == KOMMVisitor.MapTo.Constructor -> {
            val destinationName = destination.simpleName.asString()
            if (destinationName == sourceName) {
                throw KOMMException("There is no mapping for $destinationName property! You can use @${MapDefault::class.simpleName} or name support annotations (e.g. @${MapFrom::class.simpleName} etc.).")
            } else {
                throw KOMMException("There is no mapping for $destinationName property! It seems you specify bad name ($sourceName) into name support annotation (e.g. @${MapFrom::class.simpleName} etc.).")
            }
        }

        else -> null
    }

    private fun getSourceProperties(source: KSType): Map<String, KSDeclaration> {
        val sourceClass = source.declaration as KSClassDeclaration
        return sourceClass.getAllProperties().associate { it.toString() to it as KSDeclaration }.toMutableMap().apply {
            putAll(
                sourceClass.getAllFunctions().filter { it.parameters.isEmpty() }
                    .associateBy { it.toString().substring(3).lowercase() })
        }
    }

    private fun getSourceWithCast(
        destinationProperty: KSPropertyDeclaration,
        sourcePropertyType: KSDeclaration?,
        config: KSAnnotation
    ): String {
        val (propertyName, propertyType) = when (sourcePropertyType) {
            is KSFunctionDeclaration -> sourcePropertyType.toString().substring(3)
                .lowercase() to sourcePropertyType.returnType!!.resolve()

            is KSPropertyDeclaration -> sourcePropertyType.toString() to sourcePropertyType.type.resolve()
            else -> throw KOMMException("There is no source property for ${destinationProperty.simpleName}")
        }

        val destinationType = destinationProperty.type.resolve()

        if (destinationType.isAssignableFrom(propertyType)) {
            return propertyName
        }
        
        if (!config.getConfigValue<Boolean>(MapConfiguration::tryAutoCast.name)) {
            throw KOMMCastException("AutoCast is turned off! You have to use @${MapConvert::class.simpleName} annotation to cast (${destinationProperty.simpleName.asString()}: $destinationType) from ($propertyName: $propertyType).")
        }

        if (propertyType.toTypeName().isNullable) {
            val destinationHasNullSubstitute =
                destinationProperty.annotations.any { it.shortName.asString() == NullSubstitute::class.simpleName }
            if (!destinationHasNullSubstitute && !config.getConfigValue<Boolean>(MapConfiguration::allowNotNullAssertion.name)) {
                throw KOMMCastException("Auto Not-Null Assertion is not allowed! You have to use @${NullSubstitute::class.simpleName} annotation for ${destinationProperty.simpleName.asString()} property.")
            }
            if (propertyType.isAssignableFrom(destinationType)) {
                return "$propertyName!!"
            }
        }

        return "$propertyName${if (propertyType.toTypeName().isNullable) "!!" else ""}.to${destinationProperty.type}()"
    }
}