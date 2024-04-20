package com.ucasoft.komm.plugins.iterable

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.reflect.KType
import kotlin.reflect.typeOf

internal class IterablePluginTests : BaseIterablePluginTests() {

    private val plugin = IterablePlugin()

    @ParameterizedTest
    @MethodSource("forCastResultArguments")
    fun forCastResult(sourceType: KType, destinationType: KType, result: Boolean) {
        plugin.forCast(buildKSType(sourceType), buildKSType(destinationType)).shouldBe(result)
    }

    @ParameterizedTest
    @MethodSource("castResultArguments")
    fun castResult(sourceName: String, sourceType: KType, destinationType: KType, hasNullSubstitute: Boolean, result: String) {
        plugin.cast(sourceName, buildKSType(sourceType), buildDestination(hasNullSubstitute), buildKSType(destinationType)).shouldBe(result)
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
            typeOf<Double>()
        )

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
            Arguments.of("mutableSourceIntList", typeOf<MutableList<Int>>(), typeOf<List<Int>>(), false, "mutableSourceIntList"),
            Arguments.of("mutableSourceIntList", typeOf<MutableList<Int>>(), typeOf<MutableList<Int>>(), false, "mutableSourceIntList"),
            Arguments.of("sourceIntList", typeOf<List<Int>>(), typeOf<List<String>>(), false, "sourceIntList.map{ it.toString() }"),
            Arguments.of("sourceStringList", typeOf<List<String>>(), typeOf<List<Int>>(), false, "sourceStringList.map{ it.toInt() }"),
            Arguments.of("sourceStringList", typeOf<List<String>>(), typeOf<Set<Int>>(), false, "sourceStringList.map{ it.toInt() }.toSet()"),
            Arguments.of("sourceStringList", typeOf<List<String>>(), typeOf<MutableList<Int>>(), false, "sourceStringList.map{ it.toInt() }.toMutableList()"),
            Arguments.of("nullableSourceIntList", typeOf<MutableList<Int>?>(), typeOf<List<Int>>(), false, "nullableSourceIntList!!"),
            Arguments.of("nullableSourceIntList", typeOf<List<Int>?>(), typeOf<List<Int>>(), true, "nullableSourceIntList"),
            Arguments.of("nullableSourceStringList", typeOf<List<String>?>(), typeOf<List<Int>>(), false, "nullableSourceStringList!!.map{ it.toInt() }"),
            Arguments.of("nullableSourceStringList", typeOf<List<String>?>(), typeOf<List<Int>>(), true, "nullableSourceStringList?.map{ it.toInt() }"),
            Arguments.of("nullableSourceStringList", typeOf<List<String>?>(), typeOf<Set<Int>>(), false, "nullableSourceStringList!!.map{ it.toInt() }.toSet()"),
            Arguments.of("nullableSourceStringList", typeOf<List<String>?>(), typeOf<Set<Int>>(), true, "nullableSourceStringList?.map{ it.toInt() }?.toSet()"),
        )
    }
}