package com.ucasoft.komm.processor

import com.squareup.kotlinpoet.TypeSpec
import com.tschuchort.compiletesting.KotlinCompilation
import com.ucasoft.komm.annotations.*
import com.ucasoft.komm.processor.exceptions.KOMMException
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.reflection.shouldHaveMemberProperty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

internal class MapFromTests: CompilationTests() {

    override val packageName = "com.test.model"

    @Test
    fun notConstructorPropertiesMapFail() {
        val sourceSpec = buildFileSpec("SourceObject", mapOf("id" to Int::class))
        val sourceObjectClassName = sourceSpec.members.filterIsInstance<TypeSpec>().first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf("otherId" to Int::class),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("$sourceObjectClassName::class")))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        generated.messages.shouldContain("${KOMMException::class.simpleName}: There is no mapping for otherId property! You can use @${MapDefault::class.simpleName} or name support annotations (e.g. @${MapFrom::class.simpleName} etc.).")
    }

    @Test
    fun noConstructorPropertiesTest() {
        val sourceSpec = buildFileSpec("SourceObject", mapOf("id" to Int::class))
        val sourceObjectClassName = sourceSpec.members.filterIsInstance<TypeSpec>().first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf("id" to Int::class),
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
}