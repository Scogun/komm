package com.ucasoft.komm.processor

import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.tschuchort.compiletesting.KotlinCompilation
import com.ucasoft.komm.annotations.*
import com.ucasoft.komm.processor.exceptions.KOMMException
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.reflection.shouldHaveMemberProperty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.test.Test

internal class MapFromTests: CompilationTests() {

    @Test
    fun notConstructorPropertiesMapFail() {
        val sourceSpec = buildFileSpec("SourceObject", mapOf("id" to PropertySpecInit(Int::class)))
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf("otherId" to PropertySpecInit(Int::class)),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("$sourceObjectClassName::class")))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        generated.messages.shouldContain("${KOMMException::class.simpleName}: There is no mapping for otherId property! You can use @${MapDefault::class.simpleName} or name support annotations (e.g. @${MapFrom::class.simpleName} etc.).")
    }

    @Test
    fun noConstructorPropertiesTest() {
        val sourceSpec = buildFileSpec("SourceObject", mapOf("id" to PropertySpecInit(Int::class)))
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf("id" to PropertySpecInit(Int::class)),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("$sourceObjectClassName::class"))),
                mapOf("otherId" to PropertySpecInit(Int::class, "%L", 10))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceObjectClassName")
        val sourceInstance = sourceClass.constructors.first().newInstance(5)
        val destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance.shouldNotBeNull()

        destinationInstance::class.shouldHaveMemberProperty("otherId") {
            it.getter.call(destinationInstance).shouldBe(10)
        }
    }

    @ParameterizedTest
    @MethodSource("mapFromArguments")
    fun checkSuccessCasting(properties: List<MapTestProperty>) {
        val sourceSpec = buildFileSpec("SourceObject", properties.associate { it.fromName to PropertySpecInit(it.fromType) })
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                properties.associate { it.toName to PropertySpecInit(
                    it.toType,
                    annotations = listOf(
                        MapFrom::class to mapOf("name = %S" to listOf(it.fromName))
                    )
                ) },
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
            destinationInstance::class.shouldHaveMemberProperty(property.toName) {
                it.getter.call(destinationInstance).shouldBe(property.toValue)
            }
        }
    }

    @Test
    fun mapFromBadNameFail() {
        val sourceSpec = buildFileSpec("SourceObject", mapOf("fromId" to PropertySpecInit(Int::class)))
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf("toId" to PropertySpecInit(
                    Int::class,
                    annotations = listOf(
                        MapFrom::class to mapOf("name = %S" to listOf("id"))
                    )
                )),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("$sourceObjectClassName::class")))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        generated.messages.shouldContain("${KOMMException::class.simpleName}: There is no mapping for toId property! It seems you specify bad name (id) into name support annotation (e.g. @${MapFrom::class.simpleName} etc.).")
    }

    class MapTestProperty(val fromName: String, fromType: KClass<*>, fromValue: Any, val toName: String, toType: KClass<*>, toValue: Any) : CastTestProperty(toName, fromType, fromValue, toType, toValue)

    companion object {

        @JvmStatic
        fun mapFromArguments(): Stream<Arguments> = Stream.of(
            Arguments.of(
                listOf(
                    MapTestProperty("fromInt", Int::class, 123, "toInt", Int::class, 123),
                    MapTestProperty("fromStr", String::class, "456", "toStr", String::class, "456")
                )
            ),
            Arguments.of(
                listOf(
                    MapTestProperty("fromStr", String::class, "123", "toByte", Byte::class, 123.toByte()),
                    MapTestProperty("fromDouble", Double::class, 456.0, "toStr", String::class, "456.0")
                )
            )
        )
    }
}