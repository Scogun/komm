package com.ucasoft.komm.processor

import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.symbol.*
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

    private val namedAnnotations = listOf(MapFrom::class.simpleName, MapConvert::class.simpleName, NullSubstitute::class.simpleName)

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val annotations =
            classDeclaration.annotations.filter { it.shortName.asString() == KOMMMap::class.simpleName }
        for (annotation in annotations) {
            val fromArgument = annotation.arguments.first { it.name?.asString() == KOMMMap::from.name }
            val configArgument = annotation.arguments.first { it.name?.asString() == KOMMMap::config.name }
            val config = configArgument.value as KSAnnotation
            val source = fromArgument.value as KSType
            val fromSourceFunctionName = "to$classDeclaration"
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
        val statementBuilder = StringBuilder("return $destination(").appendLine()
        val properties = destination.getAllProperties().groupBy { p ->
            destination.primaryConstructor?.parameters?.any { it.name == p.simpleName }
        }
        properties[true]?.forEach {
            statementBuilder.appendLine("\t${mapProperty(it, sourceProperties, config,
                MapTo.Constructor
            )},")
        }
        if (properties.containsKey(false)) {
            val noConstructorProperties = properties[false]!!.filter {
                !it.isPrivate() && it.setter != null && !it.setter!!.modifiers.contains(Modifier.PRIVATE)
            }
            if (noConstructorProperties.isNotEmpty()) {
                statementBuilder.deleteLast(2).appendLine(").also { ")
                noConstructorProperties.forEach {
                    statementBuilder.appendLine("\tit.${mapProperty(it, sourceProperties, config,
                        MapTo.Also
                    )}")
                }
                return statementBuilder.append("}").toString()
            }
        }
        return statementBuilder.deleteLast(2).append(")").toString()
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
        sourceProperties: Map<String, KSDeclaration>,
        config: KSAnnotation,
        mapTo: MapTo
    ): String {
        val sourceName = getSourceName(destination)
        val converter = findConverter(destination)
        val resolver = findResolver(destination)
        val nullSubstituteResolver = findSubstituteResolver(destination)
        return if (resolver != null) {
            "$destination = ${mapResolver(resolver, mapTo)}"
        } else if (converter != null) {
            "$destination = $converter(this).convert($sourceName)"
        } else if (nullSubstituteResolver != null) {
            "$destination = ${getSourceWithCast(destination, sourceProperties[sourceName]!!, config).trimEnd('!').replace("!!", "?")} ?: ${mapResolver(nullSubstituteResolver, mapTo)}"
        } else {
            "$destination = ${getSourceWithCast(destination, sourceProperties[sourceName]!!, config)}"
        }
    }

    private fun mapResolver(resolver: String, mapTo: MapTo) = "$resolver(${if (mapTo == MapTo.Constructor) "null" else "it"}).resolve()"

    private fun getSourceName(member: KSPropertyDeclaration): String {
        val mapFrom = member.annotations.firstOrNull { it.shortName.asString() in namedAnnotations }
        if (mapFrom != null) {
            val nameArgument = mapFrom.arguments.first { it.name?.asString() == MapFrom::name.name }
            val name = nameArgument.value.toString()
            if (name.isNotEmpty()) {
                return name
            }
        }

        return member.toString()
    }

    private fun findConverter(member: KSPropertyDeclaration) =
        findMapAnnotation(member, MapConvert::class.simpleName, MapConvert<*>::converter.name)

    private fun findResolver(member: KSPropertyDeclaration) =
        findMapAnnotation(member, MapDefault::class.simpleName, MapDefault<*>::resolver.name)

    private fun findSubstituteResolver(member: KSPropertyDeclaration): String? {
        val annotation = member.annotations.firstOrNull { it.shortName.asString() == NullSubstitute::class.simpleName }
        if (annotation != null) {
            val resolverArgument = annotation.arguments.first { it.name?.asString() == NullSubstitute::default.name }.value as KSAnnotation
            return resolverArgument.arguments.first { it.name?.asString() == MapDefault<*>::resolver.name }.value.toString()
        }

        return null
    }

    private fun findMapAnnotation(
        member: KSPropertyDeclaration,
        annotationName: String?,
        argumentName: String
    ): String? {
        val annotation = member.annotations.firstOrNull { it.shortName.asString() == annotationName }
        if (annotation != null) {
            val resolverArgument = annotation.arguments.first { it.name?.asString() == argumentName }
            return resolverArgument.value.toString()
        }

        return null
    }

    private fun getSourceWithCast(
        destinationProperty: KSPropertyDeclaration,
        sourcePropertyType: KSDeclaration,
        config: KSAnnotation
    ) : String {
        val (propertyName, propertyType) = when (sourcePropertyType) {
            is KSFunctionDeclaration -> sourcePropertyType.toString().substring(3).lowercase() to sourcePropertyType.returnType!!.resolve()
            is KSPropertyDeclaration -> sourcePropertyType.toString() to sourcePropertyType.type.resolve()
            else -> throw KOMMException("There is no source property for ${destinationProperty.simpleName}")
        }

        val destinationType = destinationProperty.type.resolve()

        if (!destinationType.isAssignableFrom(propertyType)) {
            if (!config.getConfigValue<Boolean>(MapConfiguration::tryAutoCast.name)) {
                throw KOMMCastException("AutoCast is turned off! You have to use @${MapConvert::class.simpleName} annotation to cast (${destinationProperty.simpleName.asString()}: $destinationType) from ($propertyName: $propertyType).")
            }

            if (propertyType.toTypeName().isNullable) {
                val destinationHasNullSubstitute = destinationProperty.annotations.filter { it.shortName.asString() == NullSubstitute::class.simpleName }.count() != 0
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
}

fun StringBuilder.deleteLast(length: Int) = this.delete(this.length - length, this.length - 1)!!