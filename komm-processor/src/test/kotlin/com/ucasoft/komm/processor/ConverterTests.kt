package com.ucasoft.komm.processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.tschuchort.compiletesting.KotlinCompilation
import com.ucasoft.komm.abstractions.KOMMConverter
import com.ucasoft.komm.annotations.*
import com.ucasoft.komm.processor.exceptions.KOMMException
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.reflection.shouldHaveMemberProperty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

internal class ConverterTests: SatelliteTests() {

    @Test
    fun mapConvertBadNameFail() {
        val sourceSpec = buildFileSpec("SourceObject", mapOf("fromId" to PropertySpecInit(INT)))
        val sourceType = sourceSpec.typeSpecs.first()
        val sourceObjectClassName = ClassName(packageName, sourceType.name!!)
        val converterSpec = buildConverter(sourceObjectClassName, INT, STRING, "return sourceMember.toString()")
        val converterClassName = converterSpec.typeSpecs.first().name!!
        val generated = generate(
            sourceSpec,
            converterSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    "toId" to PropertySpecInit(
                        STRING,
                        parametrizedAnnotations = listOf(
                            MapConvert::class.asTypeName().parameterizedBy(sourceObjectClassName, ClassName(packageName, converterClassName)) to mapOf(
                                "name = %S" to listOf("id"),
                                "converter = %L" to listOf("[$converterClassName::class]")
                            )
                        )
                    )
                ),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("[$sourceObjectClassName::class]")))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        generated.messages.shouldContain("${KOMMException::class.simpleName}: There is no mapping for toId property! It seems you specify bad name (id) into name support annotation (e.g. @${MapName::class.simpleName} etc.).")
    }

    @Test
    fun mapConvertSameName() {
        val propertyName = "id"
        val sourceSpec = buildFileSpec("SourceObject", mapOf(propertyName to PropertySpecInit(INT)))
        val sourceType = sourceSpec.typeSpecs.first()
        val sourceObjectClassName = ClassName(packageName, sourceType.name!!)
        val converterSpec = buildConverter(sourceObjectClassName, INT, STRING, "return sourceMember.toString()")
        val converterClassName = converterSpec.typeSpecs.first().name!!
        val generated = generate(
            sourceSpec,
            converterSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    propertyName to PropertySpecInit(
                        STRING,
                        parametrizedAnnotations = listOf(
                            MapConvert::class.asTypeName()
                                .parameterizedBy(sourceObjectClassName, ClassName(packageName, converterClassName)) to mapOf(
                                "converter = %L" to listOf("$converterClassName::class")
                            )
                        )
                    )
                ),
                listOf(
                    KOMMMap::class to mapOf(
                        "from = %L" to listOf("[$sourceObjectClassName::class]"),
                        "config = %L" to listOf("${MapConfiguration::class.simpleName}(${MapConfiguration::tryAutoCast.name} = false)")
                    )
                )
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceClass = generated.classLoader.loadClass(sourceObjectClassName.canonicalName)
        val sourceInstance = sourceClass.constructors.first().newInstance(123)
        val destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance.shouldNotBeNull()
        destinationInstance::class.shouldHaveMemberProperty(propertyName) {
            it.getter.call(destinationInstance).shouldBe("123")
        }
    }

    @Test
    fun mapConvertFromWholeSource() {
        val sourceSpec = buildFileSpec(
            "SourceObject", mapOf(
                "name" to PropertySpecInit(STRING),
                "surname" to PropertySpecInit(STRING)
            )
        )
        val sourceType = sourceSpec.typeSpecs.first()
        val sourceObjectClassName = ClassName(packageName, sourceType.name!!)
        val converterSpec =
            buildConverter(sourceObjectClassName, STRING, STRING, "return \"\${source.name} \${source.surname}\"")
        val converterClassName = converterSpec.typeSpecs.first().name!!
        val generated = generate(
            sourceSpec,
            converterSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    "fullName" to PropertySpecInit(
                        STRING,
                        parametrizedAnnotations = listOf(
                            MapConvert::class.asTypeName()
                                .parameterizedBy(sourceObjectClassName, ClassName(packageName, converterClassName)) to mapOf(
                                "name = %S" to listOf("name"),
                                "converter = %L" to listOf("$converterClassName::class")
                            )
                        )
                    )
                ),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("[$sourceObjectClassName::class]")))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceClass = generated.classLoader.loadClass(sourceObjectClassName.canonicalName)
        val sourceInstance = sourceClass.constructors.first().newInstance("John", "Doe")
        val destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance.shouldNotBeNull()
        destinationInstance::class.shouldHaveMemberProperty("fullName") {
            it.getter.call(destinationInstance).shouldBe("John Doe")
        }
    }

    private fun buildConverter(sourceType: ClassName, srcType: ClassName, destType: ClassName, statement: String) =
        buildSatellite(
            "TestConverter",
            KOMMConverter::class.asTypeName().parameterizedBy(sourceType, srcType, destType),
            "source",
            sourceType,
            "convert",
            srcType,
            destType,
            statement
        )
}