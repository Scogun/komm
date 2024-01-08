package com.ucasoft.komm.processor

import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.ucasoft.komm.annotations.KOMMMap
import com.ucasoft.komm.annotations.MapConvert
import com.ucasoft.komm.annotations.MapDefault
import com.ucasoft.komm.annotations.MapFrom
import com.ucasoft.komm.processor.exceptions.KOMMCastException
import com.ucasoft.komm.processor.exceptions.KOMMException

class KOMMVisitor(private val functions: MutableList<FunSpec>) : KSVisitorVoid() {

    enum class MapTo {

        Constructor,

        Also
    }

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
        return if (resolver != null) {
            "$destination = $resolver(${if (mapTo == MapTo.Constructor) "null" else "it"}).resolve()"
        } else if (converter != null) {
            "$destination = $converter(this).convert($sourceName)"
        } else {
            "$destination = ${getSourceWithCast(destination, sourceProperties[sourceName]!!, config)}"
        }
    }

    private fun getSourceName(member: KSPropertyDeclaration): String {
        val mapFrom =
            member.annotations.firstOrNull { it.shortName.asString() == MapFrom::class.simpleName || it.shortName.asString() == MapConvert::class.simpleName }
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

    private fun findMapAnnotation(
        member: KSPropertyDeclaration,
        annotationName: String?,
        argumentName: String
    ): String? {
        val mapDefault = member.annotations.firstOrNull { it.shortName.asString() == annotationName }
        if (mapDefault != null) {
            val resolverArgument = mapDefault.arguments.first { it.name?.asString() == argumentName }
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
            is KSFunctionDeclaration -> sourcePropertyType.toString().substring(3).lowercase() to sourcePropertyType.returnType
            is KSPropertyDeclaration -> sourcePropertyType.toString() to sourcePropertyType.type
            else -> throw KOMMException()
        }
        if (destinationProperty.type.toTypeName() != propertyType?.toTypeName()) {
            if (!config.arguments[0].value.toString().toBoolean()) {
                throw KOMMCastException()
            }

            return "$propertyName.to${destinationProperty.type}()"
        }

        return propertyName
    }
}

fun StringBuilder.deleteLast(length: Int) = this.delete(this.length - length, this.length - 1)!!