package com.ucasoft.komm.processor

import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.asClassName
import com.tschuchort.compiletesting.KotlinCompilation
import com.ucasoft.komm.annotations.KOMMMap
import com.ucasoft.komm.annotations.MapConfiguration
import com.ucasoft.komm.processor.exceptions.KOMMException
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.reflection.shouldHaveMemberProperty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.*
import java.util.stream.Stream

internal class ProcessorTests : CompilationTests() {

    @Test
    fun checkSuccessGeneration() {
        val sourceSpec = buildFileSpec("SourceObject", mapOf("id" to PropertySpecInit(INT)))
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf("id" to PropertySpecInit(INT)),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("[$sourceObjectClassName::class]")))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val `class` = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        `class`.declaredMethods.shouldBeSingleton {
            it.name.shouldBe("toDestinationObject")
        }
    }

    @ParameterizedTest
    @MethodSource("simpleMapArguments")
    fun checkSimpleObjectMapping(properties: List<TestProperty>) {
        val sourceSpec = buildFileSpec("SourceObject", properties.associate { it.name to PropertySpecInit(it.type.asClassName()) })
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                properties.associate { it.name to PropertySpecInit(it.type.asClassName()) },
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("[$sourceObjectClassName::class]")))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceObjectClassName")
        val params = properties.map { it.value }
        val sourceInstance = sourceClass.constructors.first().newInstance(*params.toTypedArray())
        val destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance.shouldNotBeNull()
        for (property in properties) {
            destinationInstance::class.shouldHaveMemberProperty(property.name) {
                it.getter.call(destinationInstance).shouldBe(property.value)
            }
        }
    }

    @ParameterizedTest
    @MethodSource("simpleMapArguments")
    fun checkMapNotConstructorProperty(properties: List<TestProperty>) {
        val notConstructorProperty = properties.last().name
        val sourceSpec = buildFileSpec("SourceObject", properties.associate { it.name to PropertySpecInit(it.type.asClassName()) })
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                properties.filter { it.name != notConstructorProperty }.associate { it.name to PropertySpecInit(it.type.asClassName()) },
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("[$sourceObjectClassName::class]"))),
                properties.filter { it.name == notConstructorProperty }.associate { it.name to it.toPropertySpecInit() }
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceObjectClassName")
        val params = properties.map { it.value }
        val sourceInstance = sourceClass.constructors.first().newInstance(*params.toTypedArray())
        val destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance.shouldNotBeNull()
        for (property in properties) {
            destinationInstance::class.shouldHaveMemberProperty(property.name) {
                it.getter.call(destinationInstance).shouldBe(property.value)
            }
        }
    }

    @Test
    fun mapJavaObject() {
        val propertyName = "symbol"
        val generated = generate(
            buildFileSpec(
                "DestinationObject",
                mapOf(propertyName to PropertySpecInit(STRING)),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("[${Currency::class.simpleName}::class]")))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceInstance = Currency.getInstance(Locale.US)
        val destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance::class.shouldHaveMemberProperty(propertyName) {
            it.getter.call(destinationInstance).shouldBe(sourceInstance.symbol)
        }
    }

    @Test
    fun convertFunctionName() {
        val convertFunctionName = "convertToDestination"
        val sourceSpec = buildFileSpec("SourceObject", mapOf("id" to PropertySpecInit(INT)))
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf("id" to PropertySpecInit(INT)),
                listOf(
                    KOMMMap::class to mapOf(
                        "from = %L" to listOf("[$sourceObjectClassName::class]"),
                        "config = %L" to listOf("${MapConfiguration::class.simpleName}(${MapConfiguration::convertFunctionName.name} = \"$convertFunctionName\")")
                    )
                )
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val `class` = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        `class`.declaredMethods.shouldBeSingleton {
            it.name.shouldBe(convertFunctionName)
        }
    }

    @ParameterizedTest
    @MethodSource("simpleMapArguments")
    fun checkSimpleObjectMappingViaTo(properties: List<TestProperty>) {
        val destinationSpec = buildFileSpec("DestinationObject", properties.associate { it.name to PropertySpecInit(it.type.asClassName()) })
        val destinationObjectClassName = destinationSpec.typeSpecs.first().name
        val sourceSpec = buildFileSpec(
            "SourceObject",
            properties.associate { it.name to PropertySpecInit(it.type.asClassName()) },
            listOf(KOMMMap::class to mapOf("to = %L" to listOf("[$destinationObjectClassName::class]"))))
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            destinationSpec)

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceObjectClassName")
        val params = properties.map { it.value }
        val sourceInstance = sourceClass.constructors.first().newInstance(*params.toTypedArray())
        val destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance.shouldNotBeNull()
        for (property in properties) {
            destinationInstance::class.shouldHaveMemberProperty(property.name) {
                it.getter.call(destinationInstance).shouldBe(property.value)
            }
        }
    }

    @Test
    fun toDestinationIsNotKotlinFails() {
        val generated = generate(
            buildFileSpec(
                "DestinationObject",
                mapOf("id" to PropertySpecInit(INT)),
                listOf(KOMMMap::class to mapOf("to = %L" to listOf("[${Currency::class.simpleName}::class]")))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        generated.messages.shouldContain("${KOMMException::class.simpleName}: The class ${Currency::class.simpleName} is not a Kotlin class! Only Kotlin classes can be mapped via `to` parameter.")
    }

    companion object {

        @JvmStatic
        fun simpleMapArguments(): Stream<Arguments> = Stream.of(
            Arguments.of(
                listOf(
                    TestProperty("id", Int::class, 123),
                    TestProperty("name", String::class, "user")
                )
            ),
            Arguments.of(
                listOf(
                    TestProperty("id", Int::class, 123),
                    TestProperty("name", String::class, "user"),
                    TestProperty("isAvailable", Boolean::class, true),
                    TestProperty("salary", Double::class, 50.0)
                )
            )
        )
    }
}