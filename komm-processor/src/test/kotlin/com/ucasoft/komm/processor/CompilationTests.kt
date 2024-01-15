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

    abstract val packageName: String

    internal fun buildFileSpec(
        className: String,
        constructorProperties: Map<String, KClass<*>>,
        classAnnotations: List<Pair<KClass<out Annotation>, Map<String, List<Any>>>> = emptyList(),
        properties: Map<String, PropertySpecInit> = emptyMap()
    ) = FileSpec
        .builder(packageName, "$className.kt")
        .addType(
            TypeSpec
                .classBuilder(className)
                .addModifiers(KModifier.DATA)
                .primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .apply {
                            constructorProperties.forEach {
                                addParameter(it.key, it.value)
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
                                .builder(it.key, it.value)
                                .initializer(it.key)
                                .build()
                        )
                    }
                    properties.forEach {
                        addProperty(
                            PropertySpec
                                .builder(it.key, it.value.type)
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

    internal class PropertySpecInit(val type: KClass<*>, val format: String, val arg: Any)
}