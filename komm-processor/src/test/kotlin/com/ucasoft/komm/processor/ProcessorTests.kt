package com.ucasoft.komm.processor

import KOMMProcessorProvider
import com.squareup.kotlinpoet.*
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspWithCompilation
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.ucasoft.komm.CompilationTests
import com.ucasoft.komm.annotations.KOMMMap
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.reflection.shouldHaveMemberProperty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass

internal class ProcessorTests : CompilationTests() {

    override val packageName = "com.text.model"

    private val properties = mapOf(
        "id" to (Int::class to Int::class),
        "strToInt" to (String::class to Int::class),
        "doubleToInt" to (Double::class to Int::class),
        "intToString" to (Int::class to String::class)
    )

    private val sourceObject = buildFileSpec("SourceObject", properties.map { it.key to it.value.first }.toMap())

    private val sourceObjectClassName = sourceObject.members.filterIsInstance<TypeSpec>().first().name

    private val destinationObject = buildFileSpec(
        "DestinationObject",
        properties.filter { it.key != "intToString"}.map { it.key to it.value.second }.toMap(),
        listOf(KOMMMap::class to mapOf("from = %L" to listOf("$sourceObjectClassName::class"))),
        properties.filter { it.key == "intToString" }.map { it.key to it.value.second }.toMap()
    )

    @Test
    fun checkSuccessGeneration() {
        val generated = generate(sourceObject, destinationObject)

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val `class` = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        `class`.declaredMethods.shouldBeSingleton {
            it.name.shouldBe("toDestinationObject")
        }
    }

    @Test
    fun checkObjectMapping() {
        val generated = generate(sourceObject, destinationObject)

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
        val sourceInstance = sourceClass.constructors.first().newInstance(*params.toTypedArray())
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
        val generated = generate(sourceObject, destinationObject)

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceObjectClassName")
        val sourceInstance = sourceClass.constructors.first().newInstance(id, strToInt, doubleToInt, intToString)
        val exception = shouldThrow<InvocationTargetException> { mappingMethod.invoke(null, sourceInstance) }
        exception.targetException.shouldBeInstanceOf<NumberFormatException>()
    }
}