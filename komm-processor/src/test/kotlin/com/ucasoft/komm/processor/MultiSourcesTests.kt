package com.ucasoft.komm.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.ucasoft.komm.annotations.KOMMMap
import io.kotest.matchers.reflection.shouldHaveMemberProperty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class MultiSourcesTests: SatelliteTests() {

    @Test
    fun multiSourcesSimpleMap() {
        val propertyName = "id"
        val firstSourceSpec = buildFileSpec("FirstSourceObject", mapOf(propertyName to PropertySpecInit(Int::class)))
        val firstSourceObjectClassName = firstSourceSpec.typeSpecs.first().name!!
        val secondSourceSpec = buildFileSpec("SecondSourceObject", mapOf(propertyName to PropertySpecInit(Int::class)))
        val secondSourceObjectClassName = secondSourceSpec.typeSpecs.first().name!!
        val generated = generate(
            firstSourceSpec,
            secondSourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf(propertyName to PropertySpecInit(Int::class)),
                listOf(
                    KOMMMap::class to mapOf("from = %L" to listOf("$firstSourceObjectClassName::class")),
                    KOMMMap::class to mapOf("from = %L" to listOf("$secondSourceObjectClassName::class")),
                )
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val firstSourceClass = generated.classLoader.loadClass("$packageName.$firstSourceObjectClassName")
        val secondSourceClass = generated.classLoader.loadClass("$packageName.$secondSourceObjectClassName")
        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        var mappingMethod = mappingClass.declaredMethods.first { it.toString().contains(firstSourceObjectClassName) }
        var sourceInstance = firstSourceClass.constructors.first().newInstance(10)
        var destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance::class.shouldHaveMemberProperty(propertyName) {
            it.getter.call(destinationInstance).shouldBe(10)
        }

        mappingMethod = mappingClass.declaredMethods.first { it.toString().contains(secondSourceObjectClassName) }
        sourceInstance = secondSourceClass.constructors.first().newInstance(20)
        destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance::class.shouldHaveMemberProperty(propertyName) {
            it.getter.call(destinationInstance).shouldBe(20)
        }
    }
}