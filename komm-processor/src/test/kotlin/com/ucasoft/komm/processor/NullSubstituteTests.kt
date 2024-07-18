package com.ucasoft.komm.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.asTypeName
import com.tschuchort.compiletesting.KotlinCompilation
import com.ucasoft.komm.abstractions.KOMMResolver
import com.ucasoft.komm.annotations.KOMMMap
import com.ucasoft.komm.annotations.MapConfiguration
import com.ucasoft.komm.annotations.MapDefault
import com.ucasoft.komm.annotations.NullSubstitute
import com.ucasoft.komm.processor.exceptions.KOMMCastException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.reflection.shouldHaveMemberProperty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.throwable.shouldHaveMessage
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import java.lang.reflect.InvocationTargetException

internal class NullSubstituteTests: SatelliteTests() {

    @Test
    fun nullAssertionFail() {
        val sourceSpec = buildFileSpec("SourceObject", mapOf("id" to PropertySpecInit(INT, isNullable = true)))
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf("id" to PropertySpecInit(INT)),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("[$sourceObjectClassName::class]")))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        generated.messages.shouldContain("${KOMMCastException::class.simpleName}: Auto Not-Null Assertion is not allowed! You have to use @${NullSubstitute::class.simpleName} annotation for id property.")
    }

    @Test
    fun allowNullAssertionRuntimeFail() {
        val sourceSpec = buildFileSpec("SourceObject", mapOf("id" to PropertySpecInit(INT, isNullable = true)))
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf("id" to PropertySpecInit(INT)),
                listOf(
                    KOMMMap::class to mapOf(
                        "from = %L" to listOf("[$sourceObjectClassName::class]"),
                        "config = %L" to listOf("${MapConfiguration::class.simpleName}(${MapConfiguration::allowNotNullAssertion.name} = true)")
                    )
                )
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceObjectClassName")
        val sourceInstance = sourceClass.constructors.first().newInstance(null)

        val exception = shouldThrow<InvocationTargetException> { mappingMethod.invoke(null, sourceInstance) }
        exception.targetException.shouldBeInstanceOf<NullPointerException>()
    }

    @Test
    fun nullSubstituteTest() {
        val sourceSpec = buildFileSpec("SourceObject", mapOf("id" to PropertySpecInit(INT, isNullable = true)))
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val resolver = buildResolver()
        val resolverClassName = resolver.typeSpecs.first().name!!
        val generated = generate(
            sourceSpec,
            resolver,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    "id" to PropertySpecInit(
                        INT,
                        annotations = listOf(
                            NullSubstitute::class to mapOf(
                                "default = %L" to listOf("${MapDefault::class.simpleName}($resolverClassName::class)")
                            )
                        )
                    )
                ),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("[$sourceObjectClassName::class]"))),
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceObjectClassName")
        var sourceInstance = sourceClass.constructors.first().newInstance(null)
        var destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance.shouldNotBeNull()

        destinationInstance::class.shouldHaveMemberProperty("id") {
            it.getter.call(destinationInstance).shouldBe(25)
        }

        sourceInstance = sourceClass.constructors.first().newInstance(10)
        destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance.shouldNotBeNull()

        destinationInstance::class.shouldHaveMemberProperty("id") {
            it.getter.call(destinationInstance).shouldBe(10)
        }
    }

    @Test
    fun nullSubstituteWithCastTest() {
        val sourceSpec = buildFileSpec("SourceObject", mapOf("id" to PropertySpecInit(STRING, isNullable = true)))
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val resolver = buildResolver()
        val resolverClassName = resolver.typeSpecs.first().name!!
        val generated = generate(
            sourceSpec,
            resolver,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    "id" to PropertySpecInit(
                        INT,
                        annotations = listOf(
                            NullSubstitute::class to mapOf(
                                "default = %L" to listOf("${MapDefault::class.simpleName}($resolverClassName::class)")
                            )
                        )
                    )
                ),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("[$sourceObjectClassName::class]"))),
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceObjectClassName")
        var sourceInstance = sourceClass.constructors.first().newInstance(null)
        var destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance.shouldNotBeNull()

        destinationInstance::class.shouldHaveMemberProperty("id") {
            it.getter.call(destinationInstance).shouldBe(25)
        }

        sourceInstance = sourceClass.constructors.first().newInstance("10")
        destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance.shouldNotBeNull()

        destinationInstance::class.shouldHaveMemberProperty("id") {
            it.getter.call(destinationInstance).shouldBe(10)
        }

        val badParam = "bad"

        sourceInstance = sourceClass.constructors.first().newInstance(badParam)
        val exception = shouldThrow<InvocationTargetException> { mappingMethod.invoke(null, sourceInstance) }
        exception.targetException.shouldBeInstanceOf<NumberFormatException>()
        exception.targetException.shouldHaveMessage("For input string: \"$badParam\"")
    }

    private fun buildResolver() = buildSatellite(
        "TestResolver",
        KOMMResolver::class.asTypeName().parameterizedBy(ClassName(packageName, "DestinationObject"), INT),
        "destination",
        ClassName(packageName, "DestinationObject").copy(true),
        "resolve",
        null,
        INT,
        "return 25"
    )
}