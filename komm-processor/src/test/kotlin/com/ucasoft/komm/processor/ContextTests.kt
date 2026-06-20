package com.ucasoft.komm.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.asTypeName
import com.tschuchort.compiletesting.KotlinCompilation
import com.ucasoft.komm.annotations.KOMMMap
import com.ucasoft.komm.annotations.MapDefault
import com.ucasoft.komm.annotations.MapConvert
import com.ucasoft.komm.processor.exceptions.KOMMException
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.reflection.shouldHaveMemberProperty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

internal class ContextTests : SatelliteTests() {

    @Test
    fun mapContextConvert() {
        val sourceSpec = buildFileSpec("SourceObject", mapOf("id" to PropertySpecInit(INT)))
        val sourceClassName = ClassName(packageName, "SourceObject")
        val contextSpec = buildFileSpec("TestContext", mapOf("prefix" to PropertySpecInit(STRING)))
        val contextClassName = ClassName(packageName, "TestContext")
        val destinationClassName = ClassName(packageName, "DestinationObject")
        val converterSpec = buildContextConverter(
            sourceClassName,
            INT,
            contextClassName,
            destinationClassName,
            STRING,
            "return \"\${context.prefix}:\$sourceMember\""
        )
        val converterClassName = ClassName(packageName, converterSpec.typeSpecs.first().name!!)
        val generated = generate(
            sourceSpec,
            contextSpec,
            converterSpec,
            buildFileSpec(
                destinationClassName.simpleName,
                mapOf(
                    "name" to PropertySpecInit(
                        STRING,
                        parametrizedAnnotations = listOf(
                            MapConvert::class.asTypeName().parameterizedBy(
                                sourceClassName,
                                destinationClassName,
                                converterClassName
                            ) to mapOf(
                                "name = %S" to listOf("id"),
                                "converter = %L" to listOf("${converterClassName.simpleName}::class")
                            )
                        )
                    )
                ),
                listOf(
                    KOMMMap::class to mapOf(
                        "from = %L" to listOf("[${sourceClassName.simpleName}::class]"),
                        "context = %L" to listOf("${contextClassName.simpleName}::class")
                    )
                )
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceClass = generated.classLoader.loadClass(sourceClassName.canonicalName)
        val contextClass = generated.classLoader.loadClass(contextClassName.canonicalName)
        val sourceInstance = sourceClass.constructors.first().newInstance(42)
        val contextInstance = contextClass.constructors.first().newInstance("account")
        val destinationInstance = mappingMethod.invoke(null, sourceInstance, contextInstance)

        destinationInstance.shouldNotBeNull()
        destinationInstance::class.shouldHaveMemberProperty("name") {
            it.getter.call(destinationInstance).shouldBe("account:42")
        }
    }

    @Test
    fun mapDefaultWithContextResolver() {
        val sourceSpec = buildFileSpec("SourceObject", mapOf("accountId" to PropertySpecInit(INT)))
        val sourceClassName = ClassName(packageName, "SourceObject")
        val contextClassName = ClassName(packageName, "TestContext")
        val mapType = ClassName("kotlin.collections", "Map").parameterizedBy(INT, STRING)
        val contextSpec = com.squareup.kotlinpoet.FileSpec
            .builder(packageName, "TestContext.kt")
            .addType(
                com.squareup.kotlinpoet.TypeSpec
                    .classBuilder(contextClassName.simpleName)
                    .primaryConstructor(
                        com.squareup.kotlinpoet.FunSpec
                            .constructorBuilder()
                            .addParameter("accounts", mapType)
                            .build()
                    )
                    .addProperty(
                        PropertySpec
                            .builder("accounts", mapType)
                            .initializer("accounts")
                            .build()
                    )
                    .build()
            )
            .build()
        val destinationClassName = ClassName(packageName, "DestinationObject")
        val resolverSpec = buildContextResolver(
            destinationClassName,
            contextClassName,
            STRING,
            "return context.accounts[7] ?: \"\""
        )
        val resolverClassName = ClassName(packageName, resolverSpec.typeSpecs.first().name!!)
        val generated = generate(
            sourceSpec,
            contextSpec,
            resolverSpec,
            buildFileSpec(
                destinationClassName.simpleName,
                mapOf(
                    "accountName" to PropertySpecInit(
                        STRING,
                        parametrizedAnnotations = listOf(
                            MapDefault::class.asTypeName().parameterizedBy(
                                resolverClassName
                            ) to mapOf(
                                "resolver = %L" to listOf("${resolverClassName.simpleName}::class")
                            )
                        )
                    )
                ),
                listOf(
                    KOMMMap::class to mapOf(
                        "from = %L" to listOf("[${sourceClassName.simpleName}::class]"),
                        "context = %L" to listOf("${contextClassName.simpleName}::class")
                    )
                )
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceClass = generated.classLoader.loadClass(sourceClassName.canonicalName)
        val contextClass = generated.classLoader.loadClass(contextClassName.canonicalName)
        val sourceInstance = sourceClass.constructors.first().newInstance(7)
        val contextInstance = contextClass.constructors.first().newInstance(mapOf(7 to "Cash"))
        val destinationInstance = mappingMethod.invoke(null, sourceInstance, contextInstance)

        destinationInstance.shouldNotBeNull()
        destinationInstance::class.shouldHaveMemberProperty("accountName") {
            it.getter.call(destinationInstance).shouldBe("Cash")
        }
    }

    @Test
    fun mapContextConvertWithoutContextFails() {
        val sourceSpec = buildFileSpec("SourceObject", mapOf("id" to PropertySpecInit(INT)))
        val sourceClassName = ClassName(packageName, "SourceObject")
        val contextSpec = buildFileSpec("TestContext", mapOf("prefix" to PropertySpecInit(STRING)))
        val contextClassName = ClassName(packageName, "TestContext")
        val destinationClassName = ClassName(packageName, "DestinationObject")
        val converterSpec = buildContextConverter(
            sourceClassName,
            INT,
            contextClassName,
            destinationClassName,
            STRING,
            "return \"\${context.prefix}:\$sourceMember\""
        )
        val converterClassName = ClassName(packageName, converterSpec.typeSpecs.first().name!!)
        val generated = generate(
            sourceSpec,
            contextSpec,
            converterSpec,
            buildFileSpec(
                destinationClassName.simpleName,
                mapOf(
                    "name" to PropertySpecInit(
                        STRING,
                        parametrizedAnnotations = listOf(
                            MapConvert::class.asTypeName().parameterizedBy(
                                sourceClassName,
                                destinationClassName,
                                converterClassName
                            ) to mapOf(
                                "name = %S" to listOf("id"),
                                "converter = %L" to listOf("${converterClassName.simpleName}::class")
                            )
                        )
                    )
                ),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("[${sourceClassName.simpleName}::class]")))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.INTERNAL_ERROR)
        generated.messages.shouldContain("${KOMMException::class.simpleName}: ${KOMMMap::class.simpleName}.context is required when using context-aware @${MapConvert::class.simpleName} or @${MapDefault::class.simpleName}.")
    }

    @Test
    fun mapDefaultWithContextResolverWithoutContextFails() {
        val sourceSpec = buildFileSpec("SourceObject", mapOf("accountId" to PropertySpecInit(INT)))
        val sourceClassName = ClassName(packageName, "SourceObject")
        val contextSpec = buildFileSpec("TestContext", mapOf("prefix" to PropertySpecInit(STRING)))
        val contextClassName = ClassName(packageName, "TestContext")
        val destinationClassName = ClassName(packageName, "DestinationObject")
        val resolverSpec = buildContextResolver(
            destinationClassName,
            contextClassName,
            STRING,
            "return context.prefix"
        )
        val resolverClassName = ClassName(packageName, resolverSpec.typeSpecs.first().name!!)
        val generated = generate(
            sourceSpec,
            contextSpec,
            resolverSpec,
            buildFileSpec(
                destinationClassName.simpleName,
                mapOf(
                    "accountName" to PropertySpecInit(
                        STRING,
                        parametrizedAnnotations = listOf(
                            MapDefault::class.asTypeName().parameterizedBy(
                                resolverClassName
                            ) to mapOf(
                                "resolver = %L" to listOf("${resolverClassName.simpleName}::class")
                            )
                        )
                    )
                ),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("[${sourceClassName.simpleName}::class]")))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.INTERNAL_ERROR)
        generated.messages.shouldContain("${KOMMException::class.simpleName}: ${KOMMMap::class.simpleName}.context is required when using context-aware @${MapConvert::class.simpleName} or @${MapDefault::class.simpleName}.")
    }
}
