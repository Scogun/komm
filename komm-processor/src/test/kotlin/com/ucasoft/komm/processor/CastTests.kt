package com.ucasoft.komm.processor

import com.squareup.kotlinpoet.TypeSpec
import com.tschuchort.compiletesting.KotlinCompilation
import com.ucasoft.komm.annotations.KOMMMap
import com.ucasoft.komm.annotations.MapConfiguration
import com.ucasoft.komm.annotations.MapConvert
import com.ucasoft.komm.processor.exceptions.KOMMCastException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.reflection.shouldHaveMemberProperty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.throwable.shouldHaveMessage
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.test.Test

internal class CastTests: CompilationTests() {

    @Test
    fun autoCastOffFail() {
        val sourceSpec = buildFileSpec("SourceObject", mapOf("id" to PropertySpecInit(Int::class)))
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf("id" to PropertySpecInit(String::class)),
                listOf(
                    KOMMMap::class to mapOf(
                        "from = %L" to listOf("$sourceObjectClassName::class"),
                        "config = %L" to listOf("${MapConfiguration::class.simpleName}(${MapConfiguration::tryAutoCast.name} = false)")
                    )
                )
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        generated.messages.shouldContain("${KOMMCastException::class.simpleName}: AutoCast is turned off! You have to use @${MapConvert::class.simpleName} annotation to cast (id: String) from (id: Int)")
    }

    @Test
    fun runtimeCastFail() {
        val sourceSpec = buildFileSpec("SourceObject", mapOf("id" to PropertySpecInit(String::class)))
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf("id" to PropertySpecInit(Int::class)),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("$sourceObjectClassName::class"))
                )
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceObjectClassName")
        val badParam = "NotInt"
        val sourceInstance = sourceClass.constructors.first().newInstance(badParam)

        val exception = shouldThrow<InvocationTargetException> { mappingMethod.invoke(null, sourceInstance) }
        exception.targetException.shouldBeInstanceOf<NumberFormatException>()
        exception.targetException.shouldHaveMessage("For input string: \"$badParam\"")
    }

    @ParameterizedTest
    @MethodSource("castMapArguments")
    fun checkSuccessCasting(properties: List<CastTestProperty>) {
        val sourceSpec = buildFileSpec("SourceObject", properties.associate { it.name to PropertySpecInit(it.fromType) })
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                properties.associate { it.name to PropertySpecInit(it.toType) },
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("$sourceObjectClassName::class")))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceObjectClassName")
        val params = properties.map { it.fromValue }
        val sourceInstance = sourceClass.constructors.first().newInstance(*params.toTypedArray())
        val destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance.shouldNotBeNull()
        for (property in properties) {
            destinationInstance::class.shouldHaveMemberProperty(property.name) {
                it.getter.call(destinationInstance).shouldBe(property.toValue)
            }
        }
    }

    @Test
    fun castJavaObjectFunction() {
        val propertyName = "numericCode"
        val generated = generate(
            buildFileSpec(
                "DestinationObject",
                mapOf(propertyName to PropertySpecInit(String::class)),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("${Currency::class.simpleName}::class")))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceInstance = Currency.getInstance(Locale.US)
        val destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance::class.shouldHaveMemberProperty(propertyName) {
            it.getter.call(destinationInstance).shouldBe(sourceInstance.numericCode.toString())
        }
    }

    companion object {

        @JvmStatic
        fun castMapArguments(): Stream<Arguments> = Stream.of(
            Arguments.of(
                listOf(
                    CastTestProperty("strToInt", String::class, "123", Int::class, 123),
                    CastTestProperty("intToStr", Int::class, 456, String::class, "456")
                )
            ),
            Arguments.of(
                listOf(
                    CastTestProperty("strToByte", String::class, "123", Byte::class, 123.toByte()),
                    CastTestProperty("doubleToStr", Double::class, 456.0, String::class, "456.0")
                )
            )
        )
    }
}