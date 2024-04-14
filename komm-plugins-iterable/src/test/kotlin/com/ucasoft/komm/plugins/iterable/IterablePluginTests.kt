package com.ucasoft.komm.plugins.iterable

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.typeOf

internal class IterablePluginTests {

    private val plugin = IterablePlugin()

    @ParameterizedTest
    @MethodSource("forCastResultArguments")
    fun forCastResult(sourceType: KType, destinationType: KType, result: Boolean) {
        plugin.forCast(buildKSType(sourceType), buildKSType(destinationType)).shouldBe(result)
    }

    @ParameterizedTest
    @MethodSource("castResultArguments")
    fun castResult(sourceName: String, sourceType: KType, destinationType: KType, hasNullSubstitute: Boolean, result: String) {
        val destinationProperty = with(mockk<KSPropertyDeclaration>()) {
            every { annotations } returns if (hasNullSubstitute) listOf(with(mockk<KSAnnotation>()) {
                every { shortName.asString() } returns "NullSubstitute"
                this
            }).asSequence() else emptySequence()
            this
        }
        plugin.cast(sourceName, buildKSType(sourceType), destinationProperty, buildKSType(destinationType)).shouldBe(result)
    }

    private fun buildKSType(kType: KType) = with(mockk<KSType>()) {
        every { declaration } returns with(mockk<KSClassDeclaration>()) {
            every { this@with.getAllSuperTypes() } returns (kType.classifier as KClass<*>).allSuperclasses.map { sc ->
                with(mockk<KSType>()) {
                    every { this@with.toClassName() } returns sc.asClassName()
                    this
                }
            }.asSequence()
            this
        }
        if (kType.arguments.isNotEmpty()) {
            val argument = kType.arguments.first().type?.classifier as KClass<*>;
            every { arguments } returns listOf(with(mockk<KSTypeArgument>()) {
                every { type } returns with(mockk<KSTypeReference>()) {
                    every { resolve() } returns with(mockk<KSType>()) {
                        every { declaration } returns with(mockk<KSClassDeclaration>()) {
                            every { qualifiedName?.asString() } returns argument.simpleName
                            this
                        }
                        every { isAssignableFrom(any()) } answers {
                            (args[0] as KSType).declaration.qualifiedName?.asString() == argument.simpleName
                        }
                        this
                    }
                    every { this@with.toString() } returns argument.simpleName!!
                    this
                }
                this
            })
        }

        every { this@with.toClassName() } returns when {
            kType.toString().contains(".MutableList") -> MUTABLE_LIST
            else -> (kType.classifier as KClass<*>).asClassName()
        }
        every { this@with.toTypeName() } answers  {
            WildcardTypeName.producerOf(kType.classifier as KClass<*>).copy(nullable = kType.isMarkedNullable)
        }
        this
    }

    companion object {

        private val trueTypes = arrayOf(
            typeOf<List<Int>>(),
            typeOf<Set<Int>>(),
            typeOf<MutableList<Int>>(),
            typeOf<MutableSet<Int>>()
        )

        private val falseTypes = arrayOf(
            typeOf<Int>(),
            typeOf<String>(),
            typeOf<Float>(),
            typeOf<Float>(),
            typeOf<Double>()
        )

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            mockkStatic(KSClassDeclaration::getAllSuperTypes)
            mockkStatic(KSType::toClassName)
        }

        @JvmStatic
        fun forCastResultArguments(): Stream<Arguments> = Stream.of(
            *trueTypes.flatMap { firstType ->
                trueTypes.map { secondType ->
                    Arguments.of(firstType, secondType, true)
                }
            }.toTypedArray(),
            *trueTypes.flatMap { firstType ->
                falseTypes.map { secondType ->
                    Arguments.of(firstType, secondType, false)
                }
            }.toTypedArray(),
            *falseTypes.flatMap { firstType ->
                trueTypes.map { secondType ->
                    Arguments.of(firstType, secondType, false)
                }
            }.toTypedArray(),
        )

        @JvmStatic
        fun castResultArguments(): Stream<Arguments> = Stream.of(
            Arguments.of("sourceIntList", typeOf<List<Int>>(), typeOf<List<Int>>(), false, "sourceIntList"),
            Arguments.of("sourceIntList", typeOf<List<Int>>(), typeOf<MutableList<Int>>(), false, "sourceIntList.toMutableList()"),
            Arguments.of("sourceIntList", typeOf<MutableList<Int>>(), typeOf<List<Int>>(), false, "sourceIntList.toList()"),
            Arguments.of("sourceIntList", typeOf<MutableList<Int>>(), typeOf<MutableList<Int>>(), false, "sourceIntList"),
            Arguments.of("sourceIntList", typeOf<List<Int>>(), typeOf<List<String>>(), false, "sourceIntList.map{ it.toString() }"),
            Arguments.of("sourceStringList", typeOf<List<String>>(), typeOf<List<Int>>(), false, "sourceStringList.map{ it.toInt() }"),
            Arguments.of("sourceStringList", typeOf<List<String>>(), typeOf<Set<Int>>(), false, "sourceStringList.map{ it.toInt() }.toSet()"),
            Arguments.of("sourceStringList", typeOf<List<String>>(), typeOf<MutableList<Int>>(), false, "sourceStringList.map{ it.toInt() }.toMutableList()"),
            Arguments.of("nullableSourceStringList", typeOf<List<String>?>(), typeOf<List<Int>>(), false, "nullableSourceStringList!!.map{ it.toInt() }"),
            Arguments.of("nullableSourceStringList", typeOf<List<String>?>(), typeOf<List<Int>>(), true, "nullableSourceStringList?.map{ it.toInt() }"),
            Arguments.of("nullableSourceStringList", typeOf<List<String>?>(), typeOf<Set<Int>>(), false, "nullableSourceStringList!!.map{ it.toInt() }.toSet()"),
            Arguments.of("nullableSourceStringList", typeOf<List<String>?>(), typeOf<Set<Int>>(), true, "nullableSourceStringList?.map{ it.toInt() }?.toSet()"),
        )
    }
}