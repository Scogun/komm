package com.ucasoft.komm.processor

import KOMMProcessorProvider
import com.squareup.kotlinpoet.*
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspWithCompilation
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.ucasoft.komm.annotations.KOMMMap
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ProcessorTests {

    @TempDir
    private lateinit var tempDir: File

    private val sourceObject = FileSpec
        .builder("com.test.model", "SourceModel.kt")
        .addType(
            TypeSpec
                .classBuilder("SourceObject")
                .addModifiers(KModifier.DATA)
                .primaryConstructor(FunSpec
                    .constructorBuilder()
                    .addParameter("id", Int::class)
                    .build())
                .addProperty(PropertySpec
                    .builder("id", Int::class)
                    .initializer("id")
                    .build())
                .build()
        )
        .build()

    private val destinationObject = FileSpec
        .builder("com.test.model", "DestinationModel.kt")
        .addType(
            TypeSpec
                .classBuilder("DestinationObject")
                .addModifiers(KModifier.DATA)
                .addAnnotation(AnnotationSpec
                    .builder(KOMMMap::class)
                    .addMember("from = %L", "${sourceObject.members.filterIsInstance<TypeSpec>().first().name}::class")
                    .build())
                .primaryConstructor(FunSpec
                    .constructorBuilder()
                    .addParameter("id", Int::class)
                    .build())
                .addProperty(PropertySpec
                    .builder("id", Int::class)
                    .initializer("id")
                    .build())
                .build()
        )
        .build()

    @Test
    fun check() {
        val generated = KotlinCompilation().apply {
            inheritClassPath = true
            kspWithCompilation = true
            sources = listOf(
                SourceFile.kotlin(sourceObject.name, sourceObject.toString()),
                SourceFile.kotlin(destinationObject.name, destinationObject.toString())
            )
            symbolProcessorProviders = listOf(KOMMProcessorProvider())
            workingDir = tempDir
        }.compile()

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val `class` = generated.classLoader.loadClass("com.test.model.MappingExtensionsKt")
        `class`.declaredMethods.shouldBeSingleton {
            it.name.shouldBe("toDestinationObject")
        }
    }
}