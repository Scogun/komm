package com.ucasoft.komm.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.asTypeName
import com.tschuchort.compiletesting.KotlinCompilation
import com.ucasoft.komm.abstractions.KOMMConverter
import com.ucasoft.komm.abstractions.KOMMResolver
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

    @Test
    fun multiSourcesMapFromFail() {
        val firstSourceSpec = buildFileSpec("FirstSourceObject", mapOf("firstId" to PropertySpecInit(Int::class)))
        val firstSourceObjectClassName = firstSourceSpec.typeSpecs.first().name!!
        val secondSourceSpec = buildFileSpec("SecondSourceObject", mapOf("secondId" to PropertySpecInit(Int::class)))
        val secondSourceObjectClassName = secondSourceSpec.typeSpecs.first().name!!
        val generated = generate(
            firstSourceSpec,
            secondSourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    "id" to PropertySpecInit(
                        Int::class,
                        annotations = listOf(
                            MapFrom::class to mapOf("name = %S" to listOf("firstId")),
                            MapFrom::class to mapOf("name = %S" to listOf("secondId"))
                        )
                    )
                ),
                listOf(
                    KOMMMap::class to mapOf("from = %L" to listOf("$firstSourceObjectClassName::class")),
                    KOMMMap::class to mapOf("from = %L" to listOf("$secondSourceObjectClassName::class")),
                )
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        generated.messages.shouldContain("${KOMMException::class.simpleName}: There are too many @${MapFrom::class.simpleName} annotations for id property could be applied for $firstSourceObjectClassName")
    }

    @Test
    fun multiSourcesMapFrom() {
        val firstSourceSpec = buildFileSpec("FirstSourceObject", mapOf("firstId" to PropertySpecInit(Int::class)))
        val firstSourceObjectClassName = firstSourceSpec.typeSpecs.first().name!!
        val secondSourceSpec = buildFileSpec("SecondSourceObject", mapOf("secondId" to PropertySpecInit(Int::class)))
        val secondSourceObjectClassName = secondSourceSpec.typeSpecs.first().name!!
        val generated = generate(
            firstSourceSpec,
            secondSourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    "id" to PropertySpecInit(
                        Int::class,
                        annotations = listOf(
                            MapFrom::class to mapOf("name = %S" to listOf("firstId")),
                            MapFrom::class to mapOf(
                                "name = %S" to listOf("secondId"),
                                "from = %L" to listOf("[SecondSourceObject::class]")
                            )
                        )
                    )
                ),
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
    fun multiSourcesConvertsAndDefault() {
        val firstSourceSpec = buildFileSpec(
            "FirstSourceObject", mapOf(
                "name" to PropertySpecInit(String::class),
                "surname" to PropertySpecInit(String::class)
            )
        )
        val firstSourceObjectClassName = firstSourceSpec.typeSpecs.first().name!!
        val secondSourceSpec = buildFileSpec("SecondSourceObject", mapOf("fullName" to PropertySpecInit(String::class)))
        val secondSourceObjectClassName = secondSourceSpec.typeSpecs.first().name!!
        val thirdSourceObject = buildFileSpec("ThirdSourceObject", mapOf("id" to PropertySpecInit(Int::class)))
        val thirdSourceObjectClassName = thirdSourceObject.typeSpecs.first().name!!
        val converter = buildConverter(
            ClassName(packageName, firstSourceObjectClassName),
            STRING,
            STRING,
            "return \"\${source.name} \${source.surname}\""
        )
        val converterClassName = converter.typeSpecs.first().name!!
        val resolver = buildResolver()
        val resolverClassName = resolver.typeSpecs.first().name!!
        val generated = generate(
            firstSourceSpec,
            secondSourceSpec,
            thirdSourceObject,
            converter,
            resolver,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    "fullName" to PropertySpecInit(
                        String::class,
                        parametrizedAnnotations = listOf(
                            MapConvert::class.asTypeName()
                                .parameterizedBy(
                                    ClassName(packageName, firstSourceObjectClassName),
                                    ClassName(packageName, converterClassName)
                                ) to mapOf(
                                "name = %S" to listOf("name"),
                                "converter = %L" to listOf("$converterClassName::class")
                            ),
                            MapDefault::class.asTypeName()
                                .parameterizedBy(ClassName(packageName, resolverClassName)) to mapOf(
                                    "resolver = %L" to listOf("$resolverClassName::class"),
                                    "from = %L" to listOf("[$thirdSourceObjectClassName::class]")
                            )
                        )
                    )
                ),
                listOf(
                    KOMMMap::class to mapOf("from = %L" to listOf("$firstSourceObjectClassName::class")),
                    KOMMMap::class to mapOf("from = %L" to listOf("$secondSourceObjectClassName::class")),
                    KOMMMap::class to mapOf("from = %L" to listOf("$thirdSourceObjectClassName::class"))
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
                "name" to PropertySpecInit(String::class),
                "surname" to PropertySpecInit(String::class)
            )
        )
        val firstSourceObjectClassName = firstSourceSpec.typeSpecs.first().name!!
        val secondSourceSpec = buildFileSpec("SecondSourceObject", mapOf("fullName" to PropertySpecInit(String::class)))
        val secondSourceObjectClassName = secondSourceSpec.typeSpecs.first().name!!
        val thirdSourceObject = buildFileSpec("ThirdSourceObject", mapOf("id" to PropertySpecInit(Int::class)))
        val thirdSourceObjectClassName = thirdSourceObject.typeSpecs.first().name!!
        val converter = buildConverter(
            ClassName(packageName, firstSourceObjectClassName),
            STRING,
            STRING,
            "return \"\${source.name} \${source.surname}\""
        )
        val converterClassName = converter.typeSpecs.first().name!!
        val resolver = buildResolver()
        val resolverClassName = resolver.typeSpecs.first().name!!
        val generated = generate(
            firstSourceSpec,
            secondSourceSpec,
            thirdSourceObject,
            converter,
            resolver,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    "fullName" to PropertySpecInit(
                        String::class,
                        parametrizedAnnotations = listOf(
                            MapConvert::class.asTypeName()
                                .parameterizedBy(
                                    ClassName(packageName, firstSourceObjectClassName),
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
                        "from = %L" to listOf("$firstSourceObjectClassName::class"),
                        "config = %L" to listOf("${MapConfiguration::class.simpleName}(${MapConfiguration::mapDefaultAsFallback.name} = true)")
                    ),
                    KOMMMap::class to mapOf(
                        "from = %L" to listOf("$secondSourceObjectClassName::class"),
                        "config = %L" to listOf("${MapConfiguration::class.simpleName}(${MapConfiguration::mapDefaultAsFallback.name} = true)")
                    ),
                    KOMMMap::class to mapOf("from = %L" to listOf("$thirdSourceObjectClassName::class"))
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

    private fun buildResolver() = buildSatellite(
        "TestResolver",
        KOMMResolver::class.asTypeName().parameterizedBy(ClassName(packageName, "DestinationObject"), STRING),
        "destination",
        ClassName(packageName, "DestinationObject").copy(true),
        "resolve",
        null,
        STRING,
        "return \"John Doe\""
    )
}