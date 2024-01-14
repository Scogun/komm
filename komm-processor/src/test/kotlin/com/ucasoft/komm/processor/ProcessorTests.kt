package com.ucasoft.komm.processor

import KOMMProcessorProvider
import com.squareup.kotlinpoet.*
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspWithCompilation
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.ucasoft.komm.annotations.KOMMMap
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.reflection.shouldHaveMemberProperty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass

internal class ProcessorTests {

    @TempDir
    private lateinit var tempDir: File

    private val packageName = "com.text.model"

    private val properties = mapOf(
        "id" to (Int::class to Int::class),
        "strToInt" to (String::class to Int::class),
        "doubleToInt" to (Double::class to Int::class),
        "intToString" to (Int::class to String::class)
    )

    private val sourceObject = FileSpec
        .builder(packageName, "SourceModel.kt")
        .addType(
            TypeSpec
                .classBuilder("SourceObject")
                .addModifiers(KModifier.DATA)
                .primaryConstructor(FunSpec
                    .constructorBuilder()
                    .apply {
                        properties.forEach {
                            addParameter(it.key, it.value.first)
                        }
                    }
                    .build())
                .apply {
                    properties.forEach {
                        addProperty(
                            PropertySpec
                                .builder(it.key, it.value.first)
                                .initializer(it.key)
                                .build()
                        )
                    }
                }
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
                .addAnnotation(
                    AnnotationSpec
                        .builder(KOMMMap::class)
                        .addMember("from = %L", "$sourceObjectClassName::class")
                        .build()
                )
                .primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .apply {
                            properties.forEach {
                                if (it.key != "intToString") {
                                    addParameter(it.key, it.value.second)
                                }
                            }
                        }
                        .build()
                )
                .apply {
                    properties.forEach {
                        if (it.key != "intToString") {
                            addProperty(
                                PropertySpec
                                    .builder(it.key, it.value.second)
                                    .initializer(it.key)
                                    .build()
                            )
                        }
                    }
                }
                .addProperty(
                    PropertySpec
                        .builder("intToString", String::class)
                        .initializer("%S", "")
                        .mutable()
                        .build()
                )
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

        val properties = mapOf(
            "id" to (125 to 125),
            "strToInt" to ("346" to 346),
            "doubleToInt" to (789.0 to 789),
            "intToString" to (987 to "987")
        )

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceObjectClassName")
        val params = properties.values.map { it.first }
        val sourceInstance = sourceClass.constructors.first().newInstance(params[0], params[1], params[2], params[3])
        val destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance.shouldNotBeNull()
        for (property in properties) {
            destinationInstance::class.shouldHaveMemberProperty(property.key) {
                it.getter.call(destinationInstance).shouldBe(property.value.second)
            }
        }
    }

    @ParameterizedTest
    @CsvSource(value = ["123,String,456.0,789"])
    fun checkObjectMappingCallFailed(id: Int, strToInt: String, doubleToInt: Double, intToString: Int) {
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

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceObjectClassName")
        val sourceInstance = sourceClass.constructors.first().newInstance(id, strToInt, doubleToInt, intToString)
        val exception = shouldThrow<InvocationTargetException> { mappingMethod.invoke(null, sourceInstance) }
        exception.targetException.shouldBeInstanceOf<NumberFormatException>()
    }
}