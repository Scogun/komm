package com.ucasoft.komm.processor

import KOMMProcessorProvider
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspWithCompilation
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.reflect.KClass

abstract class CompilationTests {

    @TempDir
    private lateinit var tempDir: File

    open val packageName: String = "com.test.model"

    internal fun buildFileSpec(
        className: String,
        constructorProperties: Map<String, PropertySpecInit>,
        classAnnotations: List<Pair<KClass<out Annotation>, Map<String, List<Any>>>> = emptyList(),
        properties: Map<String, PropertySpecInit> = emptyMap()
    ) = FileSpec
        .builder(packageName, "$className.kt")
        .addImport("com.ucasoft.komm.annotations", "MapConfiguration", "MapDefault")
        .addImport("java.util", "Currency")
        .addType(
            TypeSpec
                .classBuilder(className)
                .addModifiers(KModifier.DATA)
                .addPrimaryConstructor(constructorProperties)
                .apply {
                    classAnnotations.forEach {
                        addAnnotation(buildAnnotation(it))
                    }
                    constructorProperties.forEach {
                        addProperty(buildProperty(it))
                    }
                    properties.forEach {
                        addProperty(buildProperty(it, false))
                    }
                }
                .build()
        )
        .build()

    private fun TypeSpec.Builder.addPrimaryConstructor(constructorProperties: Map<String, PropertySpecInit>) = apply {
        primaryConstructor(
            FunSpec
                .constructorBuilder()
                .apply {
                    constructorProperties.forEach {
                        addParameter(
                            it.key, buildType(it)
                        )
                    }
                }
                .build()
        )
    }

    private fun buildProperty(property: Map.Entry<String, PropertySpecInit>, isConstructor: Boolean = true): PropertySpec {
        return PropertySpec
            .builder(property.key, buildType(property))
            .apply {
                if (isConstructor) {
                    initializer(property.key)
                } else {
                    initializer(property.value.format, property.value.arg)
                    mutable()
                }
                property.value.annotations.forEach {
                    addAnnotation(buildAnnotation(it))
                }
                property.value.parametrizedAnnotations.forEach {
                    addAnnotation(buildParameterizedAnnotation(it))
                }
            }
            .build()
    }

    private fun buildType(property: Map.Entry<String, PropertySpecInit>) =
        if (property.value.parametrizedType != null) {
            property.value.type.parameterizedBy(property.value.parametrizedType!!.asTypeName())
        } else {
            property.value.type
        }.copy(property.value.isNullable)

    private fun buildAnnotation(annotation: Pair<KClass<out Annotation>, Map<String, List<Any>>>) =
        buildAnnotation(AnnotationSpec.builder(annotation.first), annotation.second)

    private fun buildParameterizedAnnotation(annotation: Pair<ParameterizedTypeName, Map<String, List<Any>>>) =
        buildAnnotation(AnnotationSpec.builder(annotation.first), annotation.second)

    private fun buildAnnotation(builder: AnnotationSpec.Builder, members: Map<String, List<Any>>) = builder.apply {
        members.forEach {
            addMember(it.key, *it.value.toTypedArray())
        }
    }.build()


    fun generate(vararg fileSpec: FileSpec) = KotlinCompilation().apply {
        inheritClassPath = true
        kspWithCompilation = true
        sources = fileSpec.map { SourceFile.kotlin(it.name, it.toString()) }
        symbolProcessorProviders = listOf(KOMMProcessorProvider())
        workingDir = tempDir
    }.compile()


    internal open class TestProperty(val name: String, val type: KClass<*>, val value: Any) {

        fun toPropertySpecInit() = PropertySpecInit(type.asClassName(), if (value is String) "%S" else "%L", value)
    }

    internal open class CastTestProperty(
        name: String,
        val fromType: KClass<*>,
        val fromValue: Any,
        val toType: KClass<*>,
        val toValue: Any
    ) : TestProperty(name, toType, toValue)

    internal class MapTestProperty(
        val fromName: String,
        fromType: KClass<*>,
        fromValue: Any,
        val toName: String,
        toType: KClass<*>,
        toValue: Any
    ) : CastTestProperty(toName, fromType, fromValue, toType, toValue)

    internal class PropertySpecInit(
        val type: ClassName,
        val format: String = "",
        val arg: Any? = null,
        val isNullable: Boolean = false,
        val annotations: List<Pair<KClass<out Annotation>, Map<String, List<Any>>>> = emptyList(),
        val parametrizedAnnotations: List<Pair<ParameterizedTypeName, Map<String, List<Any>>>> = emptyList(),
        val parametrizedType: KClass<*>? = null
    )
}