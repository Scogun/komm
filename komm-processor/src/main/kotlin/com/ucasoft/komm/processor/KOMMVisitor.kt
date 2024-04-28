package com.ucasoft.komm.processor

import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.ucasoft.komm.annotations.*
import com.ucasoft.komm.plugins.KOMMCastPlugin
import com.ucasoft.komm.plugins.KOMMPlugin
import com.ucasoft.komm.processor.extensions.getConfigValue
import kotlin.reflect.KClass

class KOMMVisitor(
    private val functions: MutableList<FunSpec>,
    private val plugins: Map<KClass<out KOMMPlugin>, List<Class<*>>>,
    private val logger: KSPLogger
) : KSVisitorVoid() {

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
                    .receiver(source.toTypeName())
                    .returns(classDeclaration.toClassName())
                    .addStatement(buildStatement(source, classDeclaration, config))
                    .build()
            )
        }
    }

    private fun buildStatement(source: KSType, destination: KSClassDeclaration, config: KSAnnotation): String {
        val castPlugins = plugins[KOMMCastPlugin::class]
            ?.map { it.getDeclaredConstructor().newInstance() }
            ?.filterIsInstance<KOMMCastPlugin>() ?: emptyList()
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