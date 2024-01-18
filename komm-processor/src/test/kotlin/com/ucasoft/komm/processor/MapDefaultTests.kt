package com.ucasoft.komm.processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.tschuchort.compiletesting.KotlinCompilation
import com.ucasoft.komm.abstractions.KOMMResolver
import com.ucasoft.komm.annotations.KOMMMap
import com.ucasoft.komm.annotations.MapDefault
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.reflection.shouldHaveMemberProperty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

internal class MapDefaultTests: CompilationTests() {

    @Test
    fun mapDefaultConstructorTest() {
        val sourceSpec = buildFileSpec("SourceObject", mapOf("id" to PropertySpecInit(Int::class)))
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val resolver = buildResolver()
        val resolverClassName = resolver.typeSpecs.first().name!!
        val generated = generate(
            sourceSpec,
            resolver,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    "id" to PropertySpecInit(Int::class),
                    "otherProperty" to PropertySpecInit(
                        String::class,
                        parametrizedAnnotations = listOf(
                            MapDefault::class.asTypeName().parameterizedBy(ClassName(packageName, resolverClassName)) to mapOf(
                                "resolver = %L" to listOf("$resolverClassName::class")
                            )
                        )
                    )
                ),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("$sourceObjectClassName::class"))),
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceObjectClassName")
        val sourceInstance = sourceClass.constructors.first().newInstance(5)
        val destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance.shouldNotBeNull()

        destinationInstance::class.shouldHaveMemberProperty("id") {
            it.getter.call(destinationInstance).shouldBe(5)
        }
        destinationInstance::class.shouldHaveMemberProperty("otherProperty") {
            it.getter.call(destinationInstance).shouldBe("I'm default! Destination is null.")
        }
    }

    @Test
    fun mapDefaultNoConstructorTest() {
        val sourceSpec = buildFileSpec("SourceObject", mapOf("id" to PropertySpecInit(Int::class)))
        val sourceObjectClassName = sourceSpec.typeSpecs.first().name
        val resolver = buildResolver()
        val resolverClassName = resolver.typeSpecs.first().name!!
        val generated = generate(
            sourceSpec,
            resolver,
            buildFileSpec(
                "DestinationObject",
                mapOf("id" to PropertySpecInit(Int::class)),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("$sourceObjectClassName::class"))),
                mapOf(
                    "otherProperty" to PropertySpecInit(
                        String::class,
                        "%S",
                        "I'm from destination!",
                        parametrizedAnnotations = listOf(
                            MapDefault::class.asTypeName()
                                .parameterizedBy(ClassName(packageName, resolverClassName)) to mapOf(
                                "resolver = %L" to listOf("$resolverClassName::class")
                            )
                        )
                    )
                )
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val mappingMethod = mappingClass.declaredMethods.first()
        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceObjectClassName")
        val sourceInstance = sourceClass.constructors.first().newInstance(5)
        val destinationInstance = mappingMethod.invoke(null, sourceInstance)

        destinationInstance.shouldNotBeNull()

        destinationInstance::class.shouldHaveMemberProperty("id") {
            it.getter.call(destinationInstance).shouldBe(5)
        }
        destinationInstance::class.shouldHaveMemberProperty("otherProperty") {
            it.getter.call(destinationInstance).shouldBe("I'm default! Destination is DestinationObject(id=5).")
        }
    }

    private fun buildResolver() = FileSpec
        .builder(packageName, "TestResolver.kt")
        .addType(
            TypeSpec
                .classBuilder("TestResolver")
                .superclass(KOMMResolver::class.asTypeName().parameterizedBy(ClassName(packageName, "DestinationObject"), STRING))
                .addSuperclassConstructorParameter("destination")
                .primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .addParameter("destination", ClassName(packageName, "DestinationObject").copy(true))
                        .build()
                )
                .addFunction(
                    FunSpec
                        .builder("resolve")
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(STRING)
                        .addStatement("return \"I'm default! Destination is \${destination}.\"")
                        .build()
                )
                .build()
        )
        .build()
}