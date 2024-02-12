package com.ucasoft.komm.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.ucasoft.komm.annotations.KOMMMap
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.reflection.shouldHaveMemberProperty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

internal class CollectionsTests: CompilationTests() {

    @Test
    fun mapSimilarCollections() {
        val sourceSpec = buildFileSpec(
            "SourceObject",
            mapOf(
                "intList" to PropertySpecInit(List::class, parametrizedType = Int::class)
            )
        )
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    "intList" to PropertySpecInit(List::class, parametrizedType = Int::class),
                ),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("$sourceObjectClassName::class")))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceObjectClassName")
        val parameter = listOf(1, 2, 3)
        val sourceInstance = sourceClass.constructors.first().newInstance(parameter)
        val destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance.shouldNotBeNull()
        destinationInstance::class.shouldHaveMemberProperty("intList") {
            it.getter.call(destinationInstance).shouldBe(parameter)
        }
    }

    @Test
    fun mapMutableToImmutable() {
        val sourceSpec = buildFileSpec(
            "SourceObject",
            mapOf(
                "intList" to PropertySpecInit(MutableList::class, parametrizedType = Int::class)
            )
        )
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    "intList" to PropertySpecInit(List::class, parametrizedType = Int::class),
                ),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("$sourceObjectClassName::class")))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceObjectClassName")
        val parameter = listOf(1, 2, 3)
        val sourceInstance = sourceClass.constructors.first().newInstance(parameter)
        val destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance.shouldNotBeNull()
        destinationInstance::class.shouldHaveMemberProperty("intList") {
            it.getter.call(destinationInstance).shouldBe(parameter)
        }
    }

    @Test
    fun mapImmutableToMutable() {
        val sourceSpec = buildFileSpec(
            "SourceObject",
            mapOf(
                "intList" to PropertySpecInit(List::class, parametrizedType = Int::class)
            )
        )
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    "intList" to PropertySpecInit(MutableList::class, parametrizedType = Int::class),
                ),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("$sourceObjectClassName::class")))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceObjectClassName")
        val parameter = listOf(1, 2, 3)
        val sourceInstance = sourceClass.constructors.first().newInstance(parameter)
        val destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance.shouldNotBeNull()
        destinationInstance::class.shouldHaveMemberProperty("intList") {
            it.getter.call(destinationInstance).shouldBe(parameter)
        }
    }
}