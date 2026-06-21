package com.ucasoft.komm.processor

import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.ucasoft.komm.annotations.*
import com.ucasoft.komm.plugins.KOMMCastPlugin
import com.ucasoft.komm.plugins.KOMMPlugin
import com.ucasoft.komm.plugins.KOMMTypePlugin
import com.ucasoft.komm.plugins.exceptions.KOMMPluginsException
import com.ucasoft.komm.processor.exceptions.KOMMException
import com.ucasoft.komm.processor.extensions.getConfigValue
import kotlin.reflect.KClass

class KOMMVisitor(
    private val functions: MutableList<FunSpec>,
    private val imports: MutableMap<String, List<String>>,
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

    enum class Direction {

            From,

            To
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val annotations =
            classDeclaration.annotations.filter { it.shortName.asString() == KOMMMap::class.simpleName }
        for (annotation in annotations) {
            val configArgument = annotation.arguments.first { it.name?.asString() == KOMMMap::config.name }
            val config = configArgument.value as KSAnnotation
            val context = annotation.arguments
                .first { it.name?.asString() == KOMMMap::context.name }
                .value as? KSType
            val effectiveContext = context?.takeUnless { it.toClassName() == Unit::class.asClassName() }
            val fromArgument = annotation.arguments.first { it.name?.asString() == KOMMMap::from.name }.value as ArrayList<KSType>
            val toArgument = annotation.arguments.first { it.name?.asString() == KOMMMap::to.name }.value as ArrayList<KSType>
            fromArgument.forEach {
                syncImports(classDeclaration.asStarProjectedType(), it, imports)
                functions.add(buildFunction(classDeclaration.asStarProjectedType(), it, Direction.From, config, effectiveContext))
            }
            toArgument.forEach {
                if (!it.isKotlinClass()) {
                    throw KOMMException("The class ${it.toClassName().simpleName} is not a Kotlin class! Only Kotlin classes can be mapped via `to` parameter.")
                }
                syncImports(it, classDeclaration.asStarProjectedType(), imports)
                functions.add(buildFunction(it, classDeclaration.asStarProjectedType(), Direction.To, config, effectiveContext))
            }
        }
    }

    private fun syncImports(destination: KSType, source: KSType, imports: MutableMap<String, List<String>>) {
        val destinationClassName = destination.toClassName()
        val sourceClassName = source.toClassName()
        if (
            sourceClassName.packageName != destinationClassName.packageName &&
            sourceClassName.simpleName != destinationClassName.simpleName
        ) {
            imports[sourceClassName.packageName] = imports[sourceClassName.packageName].orEmpty() + sourceClassName.simpleName
        }
    }

    private fun buildFunction(
        destination: KSType,
        source: KSType,
        direction: Direction,
        config: KSAnnotation,
        context: KSType?
    ) : FunSpec {
        val convertFunctionName = config.getConfigValue<String>(MapConfiguration::convertFunctionName.name)
        val fromSourceFunctionName = convertFunctionName.ifEmpty { "to${destination.toClassName().simpleName}" }
        val nullableContext = config.getConfigValue<Boolean>(MapConfiguration::nullableContext.name)
        return FunSpec.builder(fromSourceFunctionName)
            .receiver(getSourceName(source))
            .apply {
                if (context != null) {
                    val contextParameterBuilder = ParameterSpec
                        .builder("kommContext", context.toTypeName().copy(nullable = nullableContext))
                    if (nullableContext) {
                        contextParameterBuilder.defaultValue("null")
                    }
                    addParameter(contextParameterBuilder.build())
                }
            }
            .returns(destination.toClassName())
            .addCode(buildStatement(source, destination.declaration as KSClassDeclaration, direction, config, context))
            .build()
    }

    private fun getSourceName(source: KSType): TypeName {
        val suitedPlugins = typePlugins
            .filterIsInstance<KOMMTypePlugin>()
            .filter { it.forType(source) }

        if (suitedPlugins.count() > 1) {
            throw KOMMPluginsException("There are more than one plugin for type ${source.toClassName().simpleName}")
        } else if (suitedPlugins.count() == 1) {
            return suitedPlugins.first().sourceType(source).asTypeName()
        }

        return source.toTypeName()
    }

    private fun buildStatement(
        source: KSType,
        destination: KSClassDeclaration,
        direction: Direction,
        config: KSAnnotation,
        context: KSType?
    ): CodeBlock {
        val castPlugins = typePlugins.toMutableList().apply {
                addAll(plugins[KOMMCastPlugin::class]
                    ?.map { it.getDeclaredConstructor().newInstance() } ?: emptyList())
            }.filterIsInstance<KOMMCastPlugin>()

        val propertyMapper = KOMMPropertyMapper(
            source,
            destination.asStarProjectedType(),
            direction,
            config,
            castPlugins,
            imports,
            context?.let { "kommContext" },
            config.getConfigValue<Boolean>(MapConfiguration::nullableContext.name)
        )
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

        val statement = buildString {
            appendLine("return %T(")
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

        return CodeBlock.of(statement, destination.toClassName())
    }

    private fun StringBuilder.deleteLast(length: Int) = this.delete(this.length - length, this.length - 1)!!

    private fun KSType.isKotlinClass() = declaration.origin == Origin.KOTLIN
}
