package com.ucasoft.komm.processor

import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.ucasoft.komm.annotations.*
import com.ucasoft.komm.processor.exceptions.KOMMCastException
import com.ucasoft.komm.processor.exceptions.KOMMException
import com.ucasoft.komm.processor.extensions.getConfigValue

class KOMMVisitor(private val functions: MutableList<FunSpec>) : KSVisitorVoid() {

    enum class MapTo {

        Constructor,

        Also
    }

    private val namedAnnotations =
        listOf(MapFrom::class.simpleName, MapConvert::class.simpleName, NullSubstitute::class.simpleName)

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val annotations =
            classDeclaration.annotations.filter { it.shortName.asString() == KOMMMap::class.simpleName }
        for (annotation in annotations) {
            val fromArgument = annotation.arguments.first { it.name?.asString() == KOMMMap::from.name }
            val configArgument = annotation.arguments.first { it.name?.asString() == KOMMMap::config.name }
            val config = configArgument.value as KSAnnotation
            val source = fromArgument.value as KSType
            val convertFunctionName = config.getConfigValue<String>(MapConfiguration::convertFunctionName.name)
            val fromSourceFunctionName = convertFunctionName.ifEmpty { "to$classDeclaration" }
            functions.add(
                FunSpec.builder(fromSourceFunctionName)
                    .receiver(source.toTypeName())
                    .returns(classDeclaration.toClassName())
                    .addStatement(buildStatement(source, classDeclaration, config))
                    .build()
            )
        }
    }

    private fun buildStatement(source: KSType, destination: KSClassDeclaration, config: KSAnnotation): String {
        val sourceProperties = getSourceProperties(source)
        val properties = destination.getAllProperties().groupBy { p ->
            destination.primaryConstructor?.parameters?.any { it.name == p.simpleName }
        }

        val constructorProperties = properties[true]?.mapNotNull {
            mapProperty(it, source, sourceProperties, config, MapTo.Constructor)
        }

        val noConstructorProperties = properties[false]?.filter {
            !it.isPrivate() && it.setter != null && !it.setter!!.modifiers.contains(Modifier.PRIVATE)
        }?.mapNotNull {
            mapProperty(it, source, sourceProperties, config, MapTo.Also)
        }

        return buildString {
            appendLine("return $destination(")
            constructorProperties?.forEach { appendLine("\t$it,") }
            deleteLast(2)
            if (noConstructorProperties.isNullOrEmpty()) {
                append(")")
            } else {
                appendLine(").also { ")
                noConstructorProperties.forEach { appendLine("\tit.$it") }
                append("}")
            }
        }
    }

    private fun getSourceProperties(source: KSType): Map<String, KSDeclaration> {
        val sourceClass = source.declaration as KSClassDeclaration
        return sourceClass.getAllProperties().associate { it.toString() to it as KSDeclaration }.toMutableMap().apply {
            putAll(
                sourceClass.getAllFunctions().filter { it.parameters.isEmpty() }
                    .associateBy { it.toString().substring(3).lowercase() })
        }
    }

    private fun mapProperty(
        destination: KSPropertyDeclaration,
        source: KSType,
        sourceProperties: Map<String, KSDeclaration>,
        config: KSAnnotation,
        mapTo: MapTo
    ): String? {
        val resolver = findResolver(source, destination)
        if (!config.getConfigValue<Boolean>(MapConfiguration::mapDefaultAsFallback.name) && resolver != null) {
            return "$destination = ${mapResolver(resolver, mapTo)}"
        }

        val sourceName = getSourceName(source, destination)
        if (!sourceProperties.containsKey(sourceName)) {
            return handleNoSourceProperty(resolver, destination, sourceName, mapTo)
        }

        val converter = findConverter(source, destination)
        val nullSubstituteResolver = findSubstituteResolver(source, destination)
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

    private fun mapResolver(resolver: String, mapTo: MapTo) =
        "$resolver(${if (mapTo == MapTo.Constructor) "null" else "it"}).resolve()"

    private fun getSourceName(source: KSType, member: KSPropertyDeclaration): String {
        val mapFroms = member.annotations.filter { it.shortName.asString() in namedAnnotations }
            .associateWith(::associateWithFrom)

        val mapFrom = filterAnnotationsBySource(source.toClassName(), mapFroms, member)

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
        mapTo: MapTo
    ) = when {
        resolver != null -> "$destination = ${mapResolver(resolver, mapTo)}"
        mapTo == MapTo.Constructor -> {
            val destinationName = destination.simpleName.asString()
            if (destinationName == sourceName) {
                throw KOMMException("There is no mapping for $destinationName property! You can use @${MapDefault::class.simpleName} or name support annotations (e.g. @${MapFrom::class.simpleName} etc.).")
            } else {
                throw KOMMException("There is no mapping for $destinationName property! It seems you specify bad name ($sourceName) into name support annotation (e.g. @${MapFrom::class.simpleName} etc.).")
            }
        }

        else -> null
    }

    private fun findResolver(source: KSType, member: KSPropertyDeclaration) =
        findMapAnnotation(
            source.toClassName(),
            member,
            MapDefault::class.simpleName,
            MapDefault<*>::resolver.name
        )

    private fun findConverter(source: KSType, member: KSPropertyDeclaration) =
        findMapAnnotation(
            source.toClassName(),
            member,
            MapConvert::class.simpleName,
            MapConvert<*, *>::converter.name
        )

    private fun findSubstituteResolver(source: KSType, member: KSPropertyDeclaration): String? {
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

    private fun associateWithFrom(item: KSAnnotation): List<ClassName> {
        val fromArgument = item.arguments.firstOrNull { it.name?.asString() == MapFrom::from.name }
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

        if (!destinationType.isAssignableFrom(propertyType)) {
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

        return propertyName
    }

    private fun StringBuilder.deleteLast(length: Int) = this.delete(this.length - length, this.length - 1)!!
}