package com.ucasoft.komm.processor

import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.ucasoft.komm.annotations.*
import com.ucasoft.komm.plugins.KOMMCastPlugin
import com.ucasoft.komm.plugins.KOMMPlugin
import com.ucasoft.komm.plugins.KOMMTypePlugin
import com.ucasoft.komm.plugins.exceptions.KOMMPluginsException
import com.ucasoft.komm.processor.extensions.getConfigValue
import kotlin.reflect.KClass

class KOMMVisitor(
    private val functions: MutableList<FunSpec>,
    private val plugins: Map<KClass<out KOMMPlugin>, List<Class<*>>>
) : KSVisitorVoid() {

    private val typePlugins by lazy {
        plugins[KOMMTypePlugin::class]
            ?.map { it.getDeclaredConstructor().newInstance() } ?: emptyList()
    }

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
            val convertFunctionName = config.getConfigValue<String>(MapConfiguration::convertFunctionName.name)
            val fromSourceFunctionName = convertFunctionName.ifEmpty { "to$classDeclaration" }
            functions.add(
                FunSpec.builder(fromSourceFunctionName)
                    .receiver(getSourceName(source))
                    .returns(classDeclaration.toClassName())
                    .addStatement(buildStatement(source, classDeclaration, config))
                    .build()
            )
        }
    }

    private fun getSourceName(source: KSType): TypeName {
        val suitedPlugins = typePlugins
            .filterIsInstance<KOMMTypePlugin>()
            .filter { it.forType(source) }

        if (suitedPlugins.count() > 1) {
            throw KOMMPluginsException("There are more than one plugin for type ${source.toClassName().simpleName}")
        } else if (suitedPlugins.count() == 1) {
            return suitedPlugins.first().type(source).asTypeName()
        }

        return source.toTypeName()
    }

    private fun buildStatement(source: KSType, destination: KSClassDeclaration, config: KSAnnotation): String {
        val castPlugins = typePlugins.toMutableList().apply {
                addAll(plugins[KOMMCastPlugin::class]
                    ?.map { it.getDeclaredConstructor().newInstance() } ?: emptyList())
            }.filterIsInstance<KOMMCastPlugin>()

        val propertyMapper = KOMMPropertyMapper(source, config, castPlugins)
        val properties = destination.getAllProperties().groupBy { p ->
            destination.primaryConstructor?.parameters?.any { it.name == p.simpleName }
        }

        val constructorProperties = properties[true]?.mapNotNull {
            propertyMapper.map(it, MapTo.Constructor)
        }

        val noConstructorProperties = properties[false]?.filter {
            !it.isPrivate() && it.setter != null && !it.setter!!.modifiers.contains(Modifier.PRIVATE)
        }?.mapNotNull {
            propertyMapper.map(it, MapTo.Also)
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

    private fun StringBuilder.deleteLast(length: Int) = this.delete(this.length - length, this.length - 1)!!
}