package com.ucasoft.komm.plugins.iterable

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.reflect.KType
import kotlin.reflect.typeOf

internal class IterableStringPluginTests: BaseIterablePluginTests() {

    private val plugin = IterableStringPlugin()

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
            typeOf<Float>(),
            typeOf<Float>(),
            typeOf<Double>()
        )

        @JvmStatic
        fun forCastResultArguments(): Stream<Arguments> = Stream.of(
            *trueTypes.map {
                Arguments.of(it, typeOf<String>(), true)
            }.toTypedArray(),
            *trueTypes.flatMap { firstType ->
                falseTypes.map { secondType ->
                    Arguments.of(firstType, secondType, false)
                }
            }.toTypedArray(),
        )

        @JvmStatic
        fun castResultArguments(): Stream<Arguments> = Stream.of(
            Arguments.of("sourceIntList", typeOf<List<Int>>(), typeOf<String>(), false, "sourceIntList.joinToString()"),
            Arguments.of("nullableSourceIntList", typeOf<List<Int>?>(), typeOf<String>(), false, "nullableSourceIntList!!.joinToString()"),
            Arguments.of("nullableSourceIntList", typeOf<List<Int>?>(), typeOf<String>(), true, "nullableSourceIntList?.joinToString()"),
        )
    }
}