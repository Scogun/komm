package com.ucasoft.komm.processor

import KOMMProcessorProvider
import com.squareup.kotlinpoet.*
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
        .addImport("com.ucasoft.komm.annotations", "MapConfiguration")
        .addImport("com.ucasoft.komm.annotations", "MapDefault")
        .addImport("java.util", "Currency")
        .addType(
            TypeSpec
                .classBuilder(className)
                .addModifiers(KModifier.DATA)
                .primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .apply {
                            constructorProperties.forEach {
                                addParameter(it.key, it.value.type.asTypeName().copy(it.value.isNullable))
                            }
                        }
                        .build())
                .apply {
                    classAnnotations.forEach {
                        addAnnotation(
                            AnnotationSpec
                                .builder(it.first)
                                .apply {
                                    for (member in it.second) {
                                        addMember(member.key, *member.value.toTypedArray())
                                    }
                                }
                                .build()
                        )
                    }
                    constructorProperties.forEach {
                        addProperty(
                            PropertySpec
                                .builder(it.key, it.value.type.asTypeName().copy(it.value.isNullable))
                                .initializer(it.key)
                                .apply {
                                    it.value.annotations.forEach {
                                        addAnnotation(
                                            AnnotationSpec
                                                .builder(it.first)
                                                .apply {
                                                    it.second.forEach { (format, args) ->
                                                        addMember(format, *args.toTypedArray())
                                                    }
                                                }
                                                .build()
                                        )
                                    }
                                    it.value.parametrizedAnnotations.forEach {
                                        addAnnotation(
                                            AnnotationSpec
                                                .builder(it.first)
                                                .apply {
                                                    it.second.forEach { (format, args) ->
                                                        addMember(format, *args.toTypedArray())
                                                    }
                                                }
                                                .build()
                                        )
                                    }
                                }
                                .build()
                        )
                    }
                    properties.forEach {
                        addProperty(
                            PropertySpec
                                .builder(it.key, it.value.type.asTypeName().copy(it.value.isNullable))
                                .apply {
                                    it.value.annotations.forEach {
                                        addAnnotation(
                                            AnnotationSpec
                                                .builder(it.first)
                                                .apply {
                                                    it.second.forEach { (format, args) ->
                                                        addMember(format, *args.toTypedArray())
                                                    }
                                                }
                                                .build()
                                        )
                                    }
                                    it.value.parametrizedAnnotations.forEach {
                                        addAnnotation(
                                            AnnotationSpec
                                                .builder(it.first)
                                                .apply {
                                                    it.second.forEach { (format, args) ->
                                                        addMember(format, *args.toTypedArray())
                                                    }
                                                }
                                                .build()
                                        )
                                    }
                                }
                                .initializer(it.value.format, it.value.arg)
                                .mutable()
                                .build()
                        )
                    }
                }
                .build()
        )
        .build()

    fun generate(vararg fileSpec: FileSpec) = KotlinCompilation().apply {
        inheritClassPath = true
        kspWithCompilation = true
        sources = fileSpec.map { SourceFile.kotlin(it.name, it.toString()) }
        symbolProcessorProviders = listOf(KOMMProcessorProvider())
        workingDir = tempDir
    }.compile()


    internal open class TestProperty(val name: String, val type: KClass<*>, val value: Any) {

        fun toPropertySpecInit() = PropertySpecInit(type, if (value is String) "%S" else "%L", value)
    }

    internal open class CastTestProperty(name: String, val fromType: KClass<*>, val fromValue: Any, val toType: KClass<*>, val toValue: Any) : TestProperty(name, toType, toValue)

    internal class MapTestProperty(val fromName: String, fromType: KClass<*>, fromValue: Any, val toName: String, toType: KClass<*>, toValue: Any) : CastTestProperty(toName, fromType, fromValue, toType, toValue)

    internal class PropertySpecInit(
        val type: KClass<*>,
        val format: String = "",
        val arg: Any? = null,
        val isNullable: Boolean = false,
        val annotations: List<Pair<KClass<out Annotation>, Map<String, List<Any>>>> = emptyList(),
        val parametrizedAnnotations: List<Pair<ParameterizedTypeName, Map<String, List<Any>>>> = emptyList()
    )
}