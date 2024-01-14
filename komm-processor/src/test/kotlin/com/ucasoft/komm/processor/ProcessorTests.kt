package com.ucasoft.komm.processor

import KOMMProcessorProvider
import com.squareup.kotlinpoet.*
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspWithCompilation
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.ucasoft.komm.annotations.KOMMMap
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.reflection.shouldHaveMemberProperty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ProcessorTests {

    @TempDir
    private lateinit var tempDir: File

    private val packageName = "com.text.model"

    private val sourceObject = FileSpec
        .builder(packageName, "SourceModel.kt")
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

    private val sourceObjectClassName = sourceObject.members.filterIsInstance<TypeSpec>().first().name

    private val destinationObject = FileSpec
        .builder(packageName, "DestinationModel.kt")
        .addType(
            TypeSpec
                .classBuilder("DestinationObject")
                .addModifiers(KModifier.DATA)
                .addAnnotation(AnnotationSpec
                    .builder(KOMMMap::class)
                    .addMember("from = %L", "$sourceObjectClassName::class")
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
    fun checkSuccessGeneration() {
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

        val `class` = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        `class`.declaredMethods.shouldBeSingleton {
            it.name.shouldBe("toDestinationObject")
        }
    }

    @Test
    fun checkObjectMapping() {
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

        val expectedId = 125

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceObjectClassName")
        val sourceInstance = sourceClass.constructors.first().newInstance(expectedId)
        val destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance.shouldNotBeNull()
        destinationInstance::class.shouldHaveMemberProperty("id") {
            it.getter.call(destinationInstance).shouldBe(expectedId)
        }
    }
}