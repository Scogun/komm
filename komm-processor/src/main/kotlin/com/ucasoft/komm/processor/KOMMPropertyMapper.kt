package com.ucasoft.komm.processor

import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ksp.toTypeName
import com.ucasoft.komm.annotations.*
import com.ucasoft.komm.plugins.KOMMCastPlugin
import com.ucasoft.komm.plugins.exceptions.KOMMPluginsException
import com.ucasoft.komm.processor.exceptions.KOMMCastException
import com.ucasoft.komm.processor.exceptions.KOMMException
import com.ucasoft.komm.processor.extensions.getConfigValue

class KOMMPropertyMapper(
    source: KSType,
    destination: KSType,
    private val direction: KOMMVisitor.Direction,
    private val config: KSAnnotation,
    private val plugins: List<KOMMCastPlugin>,
    private val imports: MutableMap<String, List<String>>
) {

    private val annotationFinder = KOMMAnnotationFinder(
        when (direction) {
            KOMMVisitor.Direction.From -> source
            KOMMVisitor.Direction.To -> destination
        }
    )

    private val sourceProperties = getSourceProperties(source)
    private val embeddedSourceProperties = getEmbeddedSourceProperties(source, destination)

    fun map(destination: KSPropertyDeclaration, mapTo: KOMMVisitor.MapTo): String? {
        val resolver = annotationFinder.findResolver(destination)
        if (!config.getConfigValue<Boolean>(MapConfiguration::mapDefaultAsFallback.name) && resolver != null) {
            return "$destination = ${mapResolver(resolver, mapTo)}"
        }

        val sourceName = getMapName(destination)
        val source = getSourceProperty(sourceName)
            ?: return handleNoSourceProperty(resolver, destination, sourceName, mapTo)
        val sourceProperty = source.sourceProperty

        val converter = when (direction) {
            KOMMVisitor.Direction.From -> annotationFinder.findConverter(destination)
            KOMMVisitor.Direction.To -> (sourceProperty as? KSPropertyDeclaration)?.let {
                annotationFinder.findConverter(it)
            }
        }
        val function = when (direction) {
            KOMMVisitor.Direction.From -> annotationFinder.findFunction(destination)
            KOMMVisitor.Direction.To -> (sourceProperty as? KSPropertyDeclaration)?.let {
                annotationFinder.findFunction(it)
            }
        }
        val nullSubstituteResolver = when (direction) {
            KOMMVisitor.Direction.From -> annotationFinder.findSubstituteResolver(destination)
            KOMMVisitor.Direction.To -> (sourceProperty as? KSPropertyDeclaration)?.let {
                annotationFinder.findSubstituteResolver(it)
            }
        }
        return if (converter != null) {
            "$destination = $converter(this).convert(${getSourceAccessName(source)})"
        } else if (nullSubstituteResolver != null) {
            "$destination = ${
                getSourceWithCast(destination, source, config, function, useSafeAccess = true)
            } ?: ${mapResolver(nullSubstituteResolver, mapTo)}"
        } else {
            "$destination = ${getSourceWithCast(destination, source, config, function)}"
        }
    }

    private fun mapResolver(resolver: String, mapTo: KOMMVisitor.MapTo) =
        "$resolver(${if (mapTo == KOMMVisitor.MapTo.Constructor) "null" else "it"}).resolve()"

    private fun getSourceProperty(sourceName: String): EmbeddedSourceProperty? {
        sourceProperties[sourceName]?.let { return EmbeddedSourceProperty(null, it) }

        val embeddedProperties = embeddedSourceProperties[sourceName] ?: return null
        if (embeddedProperties.count() > 1) {
            throw KOMMException(
                "There are more than one embedded property with the same name $sourceName: ${
                    embeddedProperties.joinToString { getSourceAccessName(it) }
                }."
            )
        }

        return embeddedProperties.first()
    }

    private fun getMapNames(member: KSPropertyDeclaration): List<String> {
        val mapsFor = annotationFinder.getSuitedNamedAnnotations(member)
        val result = mutableListOf(member.toString()).apply {
            addAll(mapsFor.map { it.arguments.first { it.name?.asString() == MapName::name.name }.value.toString() }
                .filter { it.isNotEmpty() }.toMutableList())
        }

        return result
    }

    private fun getMapName(member: KSPropertyDeclaration): String {
        val mapFrom = annotationFinder.getSuitedNamedAnnotation(member)

        if (mapFrom != null) {
            val nameArgument = mapFrom.arguments.first { it.name?.asString() == MapName::name.name }
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
                throw KOMMException("There is no mapping for $destinationName property! You can use @${MapDefault::class.simpleName} or name support annotations (e.g. @${MapName::class.simpleName} etc.).")
            } else {
                throw KOMMException("There is no mapping for $destinationName property! It seems you specify bad name ($sourceName) into name support annotation (e.g. @${MapName::class.simpleName} etc.).")
            }
        }

        else -> null
    }

    private fun getSourceProperties(source: KSType): Map<String, KSDeclaration> {
        val sourceClass = source.declaration as KSClassDeclaration
        val properties = sourceClass.getAllProperties().flatMap {
            getMapNames(it).map { name -> name to it as KSDeclaration }
        }
        val result = mutableMapOf<String, KSDeclaration>()
        properties.forEach {
            if (result.containsKey(it.first)) {
                throw KOMMException("There are more than one property with the same name ${it.first} from source ${sourceClass.simpleName.asString()}.")
            }
            result[it.first] = it.second
        }
        return result.apply {
            putAll(
                sourceClass.getAllFunctions().filter { it.parameters.isEmpty() }
                    .associateBy { getSourcePropertyName(it) })
        }
    }

    private fun getEmbeddedSourceProperties(
        source: KSType,
        destination: KSType
    ): Map<String, List<EmbeddedSourceProperty>> {
        val annotationOwner = when (direction) {
            KOMMVisitor.Direction.From -> destination.declaration
            KOMMVisitor.Direction.To -> source.declaration
        } as KSClassDeclaration

        return annotationFinder.getSuitedEmbeddedAnnotations(annotationOwner)
            .flatMap { annotation ->
                val embeddedName = annotation.arguments
                    .first { it.name?.asString() == MapEmbedded::name.name }
                    .value
                    .toString()
                val embeddedSource = sourceProperties[embeddedName]
                    ?: throw KOMMException("There is no embedded mapping source $embeddedName property from source ${source.declaration.simpleName.asString()}.")
                if (embeddedSource !is KSPropertyDeclaration) {
                    throw KOMMException("Embedded mapping source $embeddedName from source ${source.declaration.simpleName.asString()} is not a property.")
                }
                val embeddedType = embeddedSource.type.resolve()
                if (embeddedType.declaration !is KSClassDeclaration) {
                    throw KOMMException("Embedded mapping source $embeddedName from source ${source.declaration.simpleName.asString()} is not a class.")
                }

                getSourceProperties(embeddedType).map { it.key to EmbeddedSourceProperty(embeddedSource, it.value) }
            }
            .groupBy({ it.first }, { it.second })
    }

    private fun getSourceWithCast(
        destinationProperty: KSPropertyDeclaration,
        source: EmbeddedSourceProperty,
        config: KSAnnotation,
        function: Pair<String, String>?,
        useSafeAccess: Boolean = false
    ): String {
        val propertyName = getSourceAccessName(source, useSafeAccess)
        val propertyType = getSourcePropertyType(source.sourceProperty)

        val destinationType = destinationProperty.type.resolve()
        val sourceIsNullable = source.isNullable
        val effectiveSourceType = if (sourceIsNullable) propertyType.makeNullable() else propertyType

        if (destinationType.isAssignableFrom(effectiveSourceType)) {
            return propertyName
        }

        if (!config.getConfigValue<Boolean>(MapConfiguration::tryAutoCast.name)) {
            throw KOMMCastException("AutoCast is turned off! You have to use @${MapConvert::class.simpleName} annotation to cast (${destinationProperty.simpleName.asString()}: $destinationType) from ($propertyName: $propertyType).")
        }

        val destinationIsNullable = destinationType.toTypeName().isNullable
        val destinationHasNullSubstitute =
            destinationProperty.annotations.any { it.shortName.asString() == NullSubstitute::class.simpleName }
        val sourceHasNullSubstitute = (source.sourceProperty as? KSPropertyDeclaration)
            ?.annotations
            ?.any { it.shortName.asString() == NullSubstitute::class.simpleName }
            ?: false
        val destinationIsNullOrNullSubstitute =
            destinationIsNullable || destinationHasNullSubstitute || sourceHasNullSubstitute
        val allowNotNullAssertion = config.getConfigValue<Boolean>(MapConfiguration::allowNotNullAssertion.name)

        if (sourceIsNullable && !destinationIsNullOrNullSubstitute && !allowNotNullAssertion) {
            throw KOMMCastException("Auto Not-Null Assertion is not allowed! You have to use @${NullSubstitute::class.simpleName} annotation for ${destinationProperty.simpleName.asString()} property.")
        }

        getSourceWithPluginCast(source, propertyName, effectiveSourceType, destinationProperty, destinationType)?.let {
            return it
        }

        if (sourceIsNullable && destinationType.isAssignableFrom(propertyType.makeNotNullable())) {
            return if (useSafeAccess) propertyName else getSourceAccessName(source, assertNotNull = true)
        }

        val shouldUseSafeCall = sourceIsNullable && (useSafeAccess || destinationIsNullable)
        val sourceAccessName = when {
            shouldUseSafeCall -> getSourceAccessName(source, useSafeAccess = true)
            sourceIsNullable -> getSourceAccessName(source, assertNotNull = true)
            else -> propertyName
        }

        val conversionFunctionName = "to${destinationType.declaration.simpleName.asString()}"
        val functionName = function?.second?.ifEmpty { conversionFunctionName } ?: conversionFunctionName
        if (function != null) {
            imports[function.first] = imports[function.first].orEmpty() + functionName
        }
        val receiverPrefix = "$sourceAccessName${if (shouldUseSafeCall) "?." else "."}"

        return "$receiverPrefix$functionName()"
    }

    private fun getSourceWithPluginCast(
        source: EmbeddedSourceProperty,
        propertyName: String,
        sourceType: KSType,
        destinationProperty: KSPropertyDeclaration,
        destinationType: KSType
    ): String? {
        val castPlugin = plugins.filter { it.forCast(sourceType, destinationType) }

        return when (castPlugin.count()) {
            0 -> null
            1 -> castPlugin.first().cast(source.sourceProperty, propertyName, sourceType, destinationProperty, destinationType)
            else -> throw KOMMPluginsException("There are more than one plugin for casting from $sourceType to $destinationType.")
        }
    }

    private fun getSourceAccessName(
        source: EmbeddedSourceProperty,
        useSafeAccess: Boolean = false,
        assertNotNull: Boolean = false
    ): String {
        val propertyName = getSourcePropertyName(source.sourceProperty)
        val propertyAssertion = if (assertNotNull && isNullable(source.sourceProperty)) "!!" else ""
        val embeddedProperty = source.embeddedProperty ?: return "$propertyName$propertyAssertion"
        val embeddedName = embeddedProperty.simpleName.asString()
        val embeddedAccess = when {
            useSafeAccess && isNullable(embeddedProperty) -> "$embeddedName?"
            assertNotNull && isNullable(embeddedProperty) -> "$embeddedName!!"
            else -> embeddedName
        }

        return "$embeddedAccess.$propertyName$propertyAssertion"
    }

    private fun getSourcePropertyName(sourceProperty: KSDeclaration) = when (sourceProperty) {
        is KSFunctionDeclaration -> sourceProperty.toString().substring(3).lowercase()
        else -> sourceProperty.simpleName.asString()
    }

    companion object {
        internal fun isNullable(sourceProperty: KSDeclaration) =
            getSourcePropertyType(sourceProperty).toTypeName().isNullable

        private fun getSourcePropertyType(sourceProperty: KSDeclaration) = when (sourceProperty) {
            is KSFunctionDeclaration -> sourceProperty.returnType!!.resolve()
            is KSPropertyDeclaration -> sourceProperty.type.resolve()
            else -> throw KOMMException("There is no source property for ${sourceProperty.simpleName}")
        }
    }

    private data class EmbeddedSourceProperty(
        val embeddedProperty: KSPropertyDeclaration?,
        val sourceProperty: KSDeclaration
    ) {
        val isNullable: Boolean
            get() = isNullable(sourceProperty) || embeddedProperty?.let(::isNullable) ?: false
    }
}
