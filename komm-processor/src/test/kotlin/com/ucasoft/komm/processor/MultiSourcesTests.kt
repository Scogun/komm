package com.ucasoft.komm.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.asTypeName
import com.tschuchort.compiletesting.KotlinCompilation
import com.ucasoft.komm.annotations.*
import com.ucasoft.komm.processor.exceptions.KOMMException
import io.kotest.matchers.reflection.shouldHaveMemberProperty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

class MultiSourcesTests: SatelliteTests() {

    @Test
    fun multiSourcesSimpleMap() {
        val propertyName = "id"
        val firstSourceSpec = buildFileSpec("FirstSourceObject", mapOf(propertyName to PropertySpecInit(INT)))
        val firstSourceObjectClassName = firstSourceSpec.typeSpecs.first().name!!
        val secondSourceSpec = buildFileSpec("SecondSourceObject", mapOf(propertyName to PropertySpecInit(INT)))
        val secondSourceObjectClassName = secondSourceSpec.typeSpecs.first().name!!
        val generated = generate(
            firstSourceSpec,
            secondSourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf(propertyName to PropertySpecInit(INT)),
                listOf(
                    KOMMMap::class to mapOf("from = %L" to listOf("[$firstSourceObjectClassName::class]")),
                    KOMMMap::class to mapOf("from = %L" to listOf("[$secondSourceObjectClassName::class]")),
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

    @Test
    fun multiSourcesSimpleMapOneAnnotation() {
        val propertyName = "id"
        val firstSourceSpec = buildFileSpec("FirstSourceObject", mapOf(propertyName to PropertySpecInit(INT)))
        val firstSourceObjectClassName = firstSourceSpec.typeSpecs.first().name!!
        val secondSourceSpec = buildFileSpec("SecondSourceObject", mapOf(propertyName to PropertySpecInit(INT)))
        val secondSourceObjectClassName = secondSourceSpec.typeSpecs.first().name!!
        val generated = generate(
            firstSourceSpec,
            secondSourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf(propertyName to PropertySpecInit(INT)),
                listOf(
                    KOMMMap::class to mapOf("from = %L" to listOf("[$firstSourceObjectClassName::class, $secondSourceObjectClassName::class]"))
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

    @Test
    fun multiSourcesMapFromFail() {
        val firstSourceSpec = buildFileSpec("FirstSourceObject", mapOf("firstId" to PropertySpecInit(INT)))
        val firstSourceObjectClassName = firstSourceSpec.typeSpecs.first().name!!
        val secondSourceSpec = buildFileSpec("SecondSourceObject", mapOf("secondId" to PropertySpecInit(INT)))
        val secondSourceObjectClassName = secondSourceSpec.typeSpecs.first().name!!
        val generated = generate(
            firstSourceSpec,
            secondSourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    "id" to PropertySpecInit(
                        INT,
                        annotations = listOf(
                            MapName::class to mapOf("name = %S" to listOf("firstId")),
                            MapName::class to mapOf("name = %S" to listOf("secondId"))
                        )
                    )
                ),
                listOf(
                    KOMMMap::class to mapOf("from = %L" to listOf("[$firstSourceObjectClassName::class]")),
                    KOMMMap::class to mapOf("from = %L" to listOf("[$secondSourceObjectClassName::class]")),
                )
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        generated.messages.shouldContain("${KOMMException::class.simpleName}: There are too many @${MapName::class.simpleName} annotations for id property could be applied for $firstSourceObjectClassName")
    }

    @Test
    fun multiSourcesMapNameFrom() {
        val firstSourceSpec = buildFileSpec("FirstSourceObject", mapOf("firstId" to PropertySpecInit(INT)))
        val firstSourceObjectClassName = firstSourceSpec.typeSpecs.first().name!!
        val secondSourceSpec = buildFileSpec("SecondSourceObject", mapOf("secondId" to PropertySpecInit(INT)))
        val secondSourceObjectClassName = secondSourceSpec.typeSpecs.first().name!!
        val generated = generate(
            firstSourceSpec,
            secondSourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    "id" to PropertySpecInit(
                        INT,
                        annotations = listOf(
                            MapName::class to mapOf("name = %S" to listOf("firstId")),
                            MapName::class to mapOf(
                                "name = %S" to listOf("secondId"),
                                "`for` = %L" to listOf("[SecondSourceObject::class]")
                            )
                        )
                    )
                ),
                listOf(
                    KOMMMap::class to mapOf("from = %L" to listOf("[$firstSourceObjectClassName::class]")),
                    KOMMMap::class to mapOf("from = %L" to listOf("[$secondSourceObjectClassName::class]")),
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

        destinationInstance::class.shouldHaveMemberProperty("id") {
            it.getter.call(destinationInstance).shouldBe(10)
        }

        mappingMethod = mappingClass.declaredMethods.first { it.toString().contains(secondSourceObjectClassName) }
        sourceInstance = secondSourceClass.constructors.first().newInstance(20)
        destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance::class.shouldHaveMemberProperty("id") {
            it.getter.call(destinationInstance).shouldBe(20)
        }
    }

    @Test
    fun multiSourcesMapTo() {
        val firstDestinationSpec = buildFileSpec("FirstDestinationObject", mapOf("firstId" to PropertySpecInit(INT)))
        val firstDestinationObjectClassName = firstDestinationSpec.typeSpecs.first().name!!
        val secondDestinationSpec = buildFileSpec("SecondDestinationObject", mapOf("secondId" to PropertySpecInit(INT)))
        val secondDestinationObjectClassName = secondDestinationSpec.typeSpecs.first().name!!
        val generated = generate(
            firstDestinationSpec,
            secondDestinationSpec,
            buildFileSpec(
                "SourceObject",
                mapOf(
                    "id" to PropertySpecInit(
                        INT,
                        annotations = listOf(
                            MapName::class to mapOf("name = %S" to listOf("firstId")),
                            MapName::class to mapOf("name = %S" to listOf("secondId"))
                        )
                    )
                ),
                listOf(
                    KOMMMap::class to mapOf("to = %L" to listOf("[$firstDestinationObjectClassName::class]")),
                    KOMMMap::class to mapOf("to = %L" to listOf("[$secondDestinationObjectClassName::class]")),
                )
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val sourceClass = generated.classLoader.loadClass("$packageName.SourceObject")
        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        var mappingMethod =
            mappingClass.declaredMethods.first { it.toString().contains(firstDestinationObjectClassName) }
        var sourceInstance = sourceClass.constructors.first().newInstance(10)
        val firstDestinationInstance = mappingMethod.invoke(null, sourceInstance)

        firstDestinationInstance::class.shouldHaveMemberProperty("firstId") {
            it.getter.call(firstDestinationInstance).shouldBe(10)
        }

        mappingMethod = mappingClass.declaredMethods.first { it.toString().contains(secondDestinationObjectClassName) }
        sourceInstance = sourceClass.constructors.first().newInstance(20)
        val secondDestinationInstance = mappingMethod.invoke(null, sourceInstance)

        secondDestinationInstance::class.shouldHaveMemberProperty("secondId") {
            it.getter.call(secondDestinationInstance).shouldBe(20)
        }
    }

    @Test
    fun multiSourcesMapNameTo() {
        val firstDestinationSpec = buildFileSpec("FirstDestinationObject", mapOf("firstId" to PropertySpecInit(INT)))
        val firstDestinationObjectClassName = firstDestinationSpec.typeSpecs.first().name!!
        val secondDestinationSpec = buildFileSpec("SecondDestinationObject", mapOf("secondId" to PropertySpecInit(INT)))
        val secondDestinationObjectClassName = secondDestinationSpec.typeSpecs.first().name!!
        val sourceSpec = buildFileSpec(
            "SourceObject",
            mapOf(
                "id" to PropertySpecInit(
                    INT,
                    annotations = listOf(
                        MapName::class to mapOf("name = %S" to listOf("firstId")),
                        MapName::class to mapOf(
                            "name = %S" to listOf("secondId"),
                            "`for` = %L" to listOf("[$secondDestinationObjectClassName::class]")
                        )
                    )
                )
            ),
            listOf(
                KOMMMap::class to mapOf("to = %L" to listOf("[$firstDestinationObjectClassName::class]")),
                KOMMMap::class to mapOf("to = %L" to listOf("[$secondDestinationObjectClassName::class]")),
            )
        )
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name!!
        val generated = generate(
            firstDestinationSpec,
            secondDestinationSpec,
            sourceSpec
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceObjectClassName")
        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        var mappingMethod =
            mappingClass.declaredMethods.first { it.toString().contains(firstDestinationObjectClassName) }
        var sourceInstance = sourceClass.constructors.first().newInstance(10)
        val firstDestinationInstance = mappingMethod.invoke(null, sourceInstance)

        firstDestinationInstance::class.shouldHaveMemberProperty("firstId") {
            it.getter.call(firstDestinationInstance).shouldBe(10)
        }

        mappingMethod = mappingClass.declaredMethods.first { it.toString().contains(secondDestinationObjectClassName) }
        sourceInstance = sourceClass.constructors.first().newInstance(20)
        val secondDestinationInstance = mappingMethod.invoke(null, sourceInstance)

        secondDestinationInstance::class.shouldHaveMemberProperty("secondId") {
            it.getter.call(secondDestinationInstance).shouldBe(20)
        }
    }

    @Test
    fun multiSourcesConvertsAndDefault() {
        val firstSourceSpec = buildFileSpec(
            "FirstSourceObject", mapOf(
                "name" to PropertySpecInit(STRING),
                "surname" to PropertySpecInit(STRING)
            )
        )
        val firstSourceObjectClassName = firstSourceSpec.typeSpecs.first().name!!
        val secondSourceSpec = buildFileSpec("SecondSourceObject", mapOf("fullName" to PropertySpecInit(STRING)))
        val secondSourceObjectClassName = secondSourceSpec.typeSpecs.first().name!!
        val thirdSourceObject = buildFileSpec("ThirdSourceObject", mapOf("id" to PropertySpecInit(INT)))
        val thirdSourceObjectClassName = thirdSourceObject.typeSpecs.first().name!!
        val destinationObjectClassName = ClassName(packageName, "DestinationObject")
        val converter = buildConverter(
            ClassName(packageName, firstSourceObjectClassName),
            STRING,
            destinationObjectClassName,
            STRING,
            "return \"\${source.name} \${source.surname}\""
        )
        val converterClassName = converter.typeSpecs.first().name!!
        val resolver = buildResolver(destinationObjectClassName, STRING, "return \"John Doe\"")
        val resolverClassName = resolver.typeSpecs.first().name!!
        val generated = generate(
            firstSourceSpec,
            secondSourceSpec,
            thirdSourceObject,
            converter,
            resolver,
            buildFileSpec(
                destinationObjectClassName.simpleName,
                mapOf(
                    "fullName" to PropertySpecInit(
                        STRING,
                        parametrizedAnnotations = listOf(
                            MapConvert::class.asTypeName()
                                .parameterizedBy(
                                    ClassName(packageName, firstSourceObjectClassName),
                                    destinationObjectClassName,
                                    ClassName(packageName, converterClassName)
                                ) to mapOf(
                                "name = %S" to listOf("name"),
                                "converter = %L" to listOf("$converterClassName::class")
                            ),
                            MapDefault::class.asTypeName()
                                .parameterizedBy(ClassName(packageName, resolverClassName)) to mapOf(
                                "resolver = %L" to listOf("$resolverClassName::class"),
                                "`for` = %L" to listOf("[$thirdSourceObjectClassName::class]")
                            )
                        )
                    )
                ),
                listOf(
                    KOMMMap::class to mapOf("from = %L" to listOf("[$firstSourceObjectClassName::class]")),
                    KOMMMap::class to mapOf("from = %L" to listOf("[$secondSourceObjectClassName::class]")),
                    KOMMMap::class to mapOf("from = %L" to listOf("[$thirdSourceObjectClassName::class]"))
                )
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val firstSourceClass = generated.classLoader.loadClass("$packageName.$firstSourceObjectClassName")
        val secondSourceClass = generated.classLoader.loadClass("$packageName.$secondSourceObjectClassName")
        val thirdSourceClass = generated.classLoader.loadClass("$packageName.$thirdSourceObjectClassName")
        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        var mappingMethod =
            mappingClass.declaredMethods.first { it.toString().contains(firstSourceObjectClassName) }
        var sourceInstance = firstSourceClass.constructors.first().newInstance("Jane", "Doe")
        var destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance::class.shouldHaveMemberProperty("fullName") {
            it.getter.call(destinationInstance).shouldBe("Jane Doe")
        }

        mappingMethod = mappingClass.declaredMethods.first { it.toString().contains(secondSourceObjectClassName) }
        sourceInstance = secondSourceClass.constructors.first().newInstance("Jane Doe")
        destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance::class.shouldHaveMemberProperty("fullName") {
            it.getter.call(destinationInstance).shouldBe("Jane Doe")
        }

        mappingMethod = mappingClass.declaredMethods.first { it.toString().contains(thirdSourceObjectClassName) }
        sourceInstance = thirdSourceClass.constructors.first().newInstance(10)
        destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance::class.shouldHaveMemberProperty("fullName") {
            it.getter.call(destinationInstance).shouldBe("John Doe")
        }
    }

    @Test
    fun multiSourcesDefaultFallback() {
        val firstSourceSpec = buildFileSpec(
            "FirstSourceObject", mapOf(
                "name" to PropertySpecInit(STRING),
                "surname" to PropertySpecInit(STRING)
            )
        )
        val firstSourceObjectClassName = firstSourceSpec.typeSpecs.first().name!!
        val secondSourceSpec = buildFileSpec("SecondSourceObject", mapOf("fullName" to PropertySpecInit(STRING)))
        val secondSourceObjectClassName = secondSourceSpec.typeSpecs.first().name!!
        val thirdSourceObject = buildFileSpec("ThirdSourceObject", mapOf("id" to PropertySpecInit(INT)))
        val thirdSourceObjectClassName = thirdSourceObject.typeSpecs.first().name!!
        val destinationObjectClassName = ClassName(packageName, "DestinationObject")
        val converter = buildConverter(
            ClassName(packageName, firstSourceObjectClassName),
            STRING,
            destinationObjectClassName,
            STRING,
            "return \"\${source.name} \${source.surname}\""
        )
        val converterClassName = converter.typeSpecs.first().name!!
        val resolver = buildResolver(destinationObjectClassName, STRING, "return \"John Doe\"")
        val resolverClassName = resolver.typeSpecs.first().name!!
        val generated = generate(
            firstSourceSpec,
            secondSourceSpec,
            thirdSourceObject,
            converter,
            resolver,
            buildFileSpec(
                destinationObjectClassName.simpleName,
                mapOf(
                    "fullName" to PropertySpecInit(
                        STRING,
                        parametrizedAnnotations = listOf(
                            MapConvert::class.asTypeName()
                                .parameterizedBy(
                                    ClassName(packageName, firstSourceObjectClassName),
                                    destinationObjectClassName,
                                    ClassName(packageName, converterClassName)
                                ) to mapOf(
                                "name = %S" to listOf("name"),
                                "converter = %L" to listOf("$converterClassName::class")
                            ),
                            MapDefault::class.asTypeName()
                                .parameterizedBy(ClassName(packageName, resolverClassName)) to mapOf(
                                "resolver = %L" to listOf("$resolverClassName::class")
                            )
                        )
                    )
                ),
                listOf(
                    KOMMMap::class to mapOf(
                        "from = %L" to listOf("[$firstSourceObjectClassName::class]"),
                        "config = %L" to listOf("${MapConfiguration::class.simpleName}(${MapConfiguration::mapDefaultAsFallback.name} = true)")
                    ),
                    KOMMMap::class to mapOf(
                        "from = %L" to listOf("[$secondSourceObjectClassName::class]"),
                        "config = %L" to listOf("${MapConfiguration::class.simpleName}(${MapConfiguration::mapDefaultAsFallback.name} = true)")
                    ),
                    KOMMMap::class to mapOf("from = %L" to listOf("[$thirdSourceObjectClassName::class]"))
                )
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val firstSourceClass = generated.classLoader.loadClass("$packageName.$firstSourceObjectClassName")
        val secondSourceClass = generated.classLoader.loadClass("$packageName.$secondSourceObjectClassName")
        val thirdSourceClass = generated.classLoader.loadClass("$packageName.$thirdSourceObjectClassName")
        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        var mappingMethod =
            mappingClass.declaredMethods.first { it.toString().contains(firstSourceObjectClassName) }
        var sourceInstance = firstSourceClass.constructors.first().newInstance("Jane", "Doe")
        var destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance::class.shouldHaveMemberProperty("fullName") {
            it.getter.call(destinationInstance).shouldBe("Jane Doe")
        }

        mappingMethod = mappingClass.declaredMethods.first { it.toString().contains(secondSourceObjectClassName) }
        sourceInstance = secondSourceClass.constructors.first().newInstance("Jane Doe")
        destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance::class.shouldHaveMemberProperty("fullName") {
            it.getter.call(destinationInstance).shouldBe("Jane Doe")
        }

        mappingMethod = mappingClass.declaredMethods.first { it.toString().contains(thirdSourceObjectClassName) }
        sourceInstance = thirdSourceClass.constructors.first().newInstance(10)
        destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance::class.shouldHaveMemberProperty("fullName") {
            it.getter.call(destinationInstance).shouldBe("John Doe")
        }
    }
}