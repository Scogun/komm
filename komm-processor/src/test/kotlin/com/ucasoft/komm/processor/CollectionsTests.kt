package com.ucasoft.komm.processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.tschuchort.compiletesting.KotlinCompilation
import com.ucasoft.komm.abstractions.KOMMResolver
import com.ucasoft.komm.annotations.KOMMMap
import com.ucasoft.komm.annotations.MapDefault
import com.ucasoft.komm.annotations.MapFrom
import com.ucasoft.komm.annotations.NullSubstitute
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.reflection.shouldHaveMemberProperty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.kotest.matchers.types.shouldBeInstanceOf
import java.lang.reflect.InvocationTargetException
import kotlin.test.Test

internal class CollectionsTests: SatelliteTests() {

    @Test
    fun mapSimilarCollections() {
        val sourceSpec = buildFileSpec(
            "SourceObject",
            mapOf(
                "intList" to PropertySpecInit(LIST, parametrizedType = Int::class)
            )
        )
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    "intList" to PropertySpecInit(LIST, parametrizedType = Int::class),
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
                "intList" to PropertySpecInit(MUTABLE_LIST, parametrizedType = Int::class)
            )
        )
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    "intList" to PropertySpecInit(LIST, parametrizedType = Int::class),
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
                "intList" to PropertySpecInit(LIST, parametrizedType = Int::class)
            )
        )
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    "intList" to PropertySpecInit(MUTABLE_LIST, parametrizedType = Int::class),
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
    fun mapDifferentTypeLists() {
        val sourceSpec = buildFileSpec(
            "SourceObject",
            mapOf(
                "someList" to PropertySpecInit(LIST, parametrizedType = Int::class)
            )
        )
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    "someList" to PropertySpecInit(LIST, parametrizedType = String::class),
                ),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("$sourceObjectClassName::class")))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun mapDifferentTypeListToMutableList() {
        val sourceSpec = buildFileSpec(
            "SourceObject",
            mapOf(
                "someList" to PropertySpecInit(LIST, parametrizedType = String::class)
            )
        )
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    "someList" to PropertySpecInit(MUTABLE_LIST, parametrizedType = Int::class),
                ),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("$sourceObjectClassName::class")))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceObjectClassName")
        val parameter = listOf("1", "2", "3")
        var sourceInstance = sourceClass.constructors.first().newInstance(parameter)
        val destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance.shouldNotBeNull()
        destinationInstance::class.shouldHaveMemberProperty("someList") {
            it.getter.call(destinationInstance).shouldBe(parameter.map { it.toInt() })
        }

        sourceInstance = sourceClass.constructors.first().newInstance(listOf("notInt"))
        val exception = shouldThrow<InvocationTargetException> {
            mappingMethod.invoke(null, sourceInstance)
        }
        exception.targetException.shouldBeInstanceOf<NumberFormatException>()
        exception.targetException.shouldHaveMessage("For input string: \"notInt\"")
    }

    @Test
    fun mapDifferentTypeNullableLists() {
        val sourceSpec = buildFileSpec(
            "SourceObject",
            mapOf(
                "someList" to PropertySpecInit(LIST, isNullable = true, parametrizedType = String::class)
            )
        )
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    "someList" to PropertySpecInit(LIST, isNullable = true, parametrizedType = Int::class),
                ),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("$sourceObjectClassName::class")))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun mapDifferentTypeNullableListToList() {
        val sourceSpec = buildFileSpec(
            "SourceObject",
            mapOf(
                "someList" to PropertySpecInit(LIST, isNullable = true, parametrizedType = String::class)
            )
        )
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val resolver = buildResolver()
        val resolverClassName = resolver.typeSpecs.first().name!!
        val generated = generate(
            sourceSpec,
            resolver,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    "someList" to PropertySpecInit(
                        LIST, annotations = listOf(
                            NullSubstitute::class to mapOf(
                                "default = %L" to listOf("${MapDefault::class.simpleName}($resolverClassName::class)")
                            )
                        ), parametrizedType = Int::class
                    ),
                ),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("$sourceObjectClassName::class")))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun mapSetToList() {
        val sourceSpec = buildFileSpec(
            "SourceObject",
            mapOf(
                "someSet" to PropertySpecInit(SET, parametrizedType = Int::class)
            )
        )
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    "someList" to PropertySpecInit(
                        LIST,
                        annotations = listOf(
                            MapFrom::class to mapOf("name = %S" to listOf("someSet"))
                        ),
                        parametrizedType = Int::class
                    ),
                ),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("$sourceObjectClassName::class")))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceObjectClassName")
        val parameter = setOf(1, 2, 3)
        val sourceInstance = sourceClass.constructors.first().newInstance(parameter)
        val destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance.shouldNotBeNull()
        destinationInstance::class.shouldHaveMemberProperty("someList") {
            it.getter.call(destinationInstance).shouldBe(parameter.toList())
        }
    }

    private fun buildResolver() = buildSatellite(
        "TestListResolver",
        KOMMResolver::class.asTypeName().parameterizedBy(ClassName(packageName, "DestinationObject"), LIST.parameterizedBy(INT)),
        "destination",
        ClassName(packageName, "DestinationObject").copy(true),
        "resolve",
        null,
        LIST.parameterizedBy(INT),
        "return listOf(1, 2, 3)"
    )
}