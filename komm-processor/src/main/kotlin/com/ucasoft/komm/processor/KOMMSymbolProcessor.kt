package com.ucasoft.komm.processor

import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import com.ucasoft.komm.annotations.KOMMMap
import com.ucasoft.komm.annotations.MapConvert
import com.ucasoft.komm.annotations.MapDefault
import com.ucasoft.komm.annotations.MapFrom
import com.ucasoft.komm.processor.exceptions.KOMMCastException

class KOMMSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(KOMMMap::class.qualifiedName!!).filterIsInstance<KSClassDeclaration>()

        if (!symbols.iterator().hasNext()) {
            return emptyList()
        }


        symbols.groupBy { it.packageName }.forEach { (pn, cs) ->
            var file = FileSpec.builder(pn.asString(), "MappingExtensions")
            val functions = mutableListOf<FunSpec>()

            cs.forEach {
                it.accept(Visitor(functions), Unit)
            }

            functions.forEach {
                file = file.addFunction(it)
            }

            file.build().writeTo(codeGenerator, false)
        }

        return symbols.filterNot { it.validate() }.toList()
    }

    enum class MapTo {

        Constructor,

        Also
    }

    inner class Visitor(private val functions: MutableList<FunSpec>) : KSVisitorVoid() {

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
                    FunSpec
                        .builder(fromSourceFunctionName)
                        .receiver(source.toTypeName())
                        .returns(classDeclaration.toClassName())
                        .addStatement(buildStatement(source, classDeclaration, config))
                        .build()
                )
            }
        }

        private fun buildStatement(source: KSType, destination: KSClassDeclaration, config: KSAnnotation): String {
            val sourceClass = source.declaration as KSClassDeclaration
            val sourceProperties = sourceClass.getAllProperties().associate { it.toString() to it as KSDeclaration }.toMutableMap().apply {  putAll(sourceClass.getAllFunctions().associateBy { it.toString().substring(3).lowercase() }) }
            var result = "return $destination(\n"
            val properties = destination.getAllProperties().groupBy { p ->
                destination.primaryConstructor?.parameters?.any { it.name == p.simpleName }
            }
            properties[true]?.forEach {
                result += "\t${mapProperty(it, sourceProperties, config, MapTo.Constructor)},\n"
            }
            if (properties.containsKey(false)) {
                val noConstructorProperties = properties[false]!!.filter {
                    !it.isPrivate() && it.setter != null && !it.setter!!.modifiers.contains(Modifier.PRIVATE)
                }
                if (noConstructorProperties.isNotEmpty()) {
                    result = "${result.trimEnd('\n', ',')}\n).also { \n"
                    noConstructorProperties.forEach {
                        result += "\tit.${mapProperty(it, sourceProperties, config, MapTo.Also)}\n"
                    }
                    return "$result}"
                }
            }
            return "${result.trimEnd('\n', ',')}\n)"
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
                "$destination = $resolver(${ if(mapTo == MapTo.Constructor) "null" else "it" }).resolve()"
            } else if (converter != null) {
                "$destination = $converter(this).convert($sourceName)"
            } else {
                "$destination = ${getSourceWithCast(destination, sourceProperties[sourceName]!!, config)}"
            }
        }

        private fun getSourceName(member: KSPropertyDeclaration) : String {
            val mapFrom = member.annotations.firstOrNull { it.shortName.asString() == MapFrom::class.simpleName || it.shortName.asString() == MapConvert::class.simpleName }
            if (mapFrom != null) {
                val nameArgument = mapFrom.arguments.first { it.name?.asString() == MapFrom::name.name }
                val name = nameArgument.value.toString()
                if (name.isNotEmpty()) {
                    return name
                }
            }

            return member.toString()
        }

        private fun findConverter(member: KSPropertyDeclaration): String? {
            val mapConverter = member.annotations.firstOrNull { it.shortName.asString() == MapConvert::class.simpleName }
            if (mapConverter != null) {
                val converterArgument = mapConverter.arguments.first { it.name?.asString() == MapConvert<*>::converter.name }
                return converterArgument.value.toString()
            }

            return null
        }

        private fun findResolver(member: KSPropertyDeclaration) : String? {
            val mapDefault = member.annotations.firstOrNull { it.shortName.asString() == MapDefault::class.simpleName }
            if (mapDefault != null) {
                val resolverArgument = mapDefault.arguments.first { it.name?.asString() == MapDefault<*>::resolver.name }
                return resolverArgument.value.toString()
            }

            return null
        }

        private fun getSourceWithCast(
            destinationProperty: KSPropertyDeclaration,
            sourcePropertyType: KSDeclaration,
            config: KSAnnotation
        ) = when (sourcePropertyType) {
            is KSPropertyDeclaration -> {
                val pureProperty = sourcePropertyType.toString()
                if (destinationProperty.type.toTypeName() != sourcePropertyType.type.toTypeName()) {
                    if (!config.arguments[0].value?.toString().toBoolean()) {
                        throw KOMMCastException()
                    }
                    "$pureProperty.to${destinationProperty.type}()"
                } else {
                    pureProperty
                }
            }
            is KSFunctionDeclaration -> sourcePropertyType.toString().substring(3).lowercase()
            else -> sourcePropertyType.toString()
        }
    }
}