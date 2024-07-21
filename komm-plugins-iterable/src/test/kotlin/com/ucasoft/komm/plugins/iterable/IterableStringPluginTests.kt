package com.ucasoft.komm.plugins.iterable

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSValueArgument
import com.ucasoft.komm.plugins.iterable.annotations.KOMMIterableString
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
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
    fun castResult(sourceName: String, sourceType: KType, destinationType: KType, hasNullSubstitute: Boolean, delimiter: String, result: String) {
        var destination = buildProperty(hasNullSubstitute)
        if (delimiter.isNotEmpty()) {
            destination = with(mockk<KSPropertyDeclaration>()) {
                every { annotations } returns destination.annotations.toMutableList().apply {
                    add(with(mockk<KSAnnotation>()) {
                        every { shortName.asString() } returns KOMMIterableString::class.simpleName.toString()
                        every { arguments } returns listOf(with(mockk<KSValueArgument>()) {
                            every { name?.asString() } returns KOMMIterableString::delimiter.name
                            every { value?.toString() } returns delimiter
                            this
                        })
                        this
                    })
                }.asSequence()
                this
            }
        }
        plugin.cast(buildProperty(false), sourceName, buildKSType(sourceType), destination, buildKSType(destinationType)).shouldBe(result)
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
            typeOf<Double>()
        )

        @JvmStatic
        fun forCastResultArguments(): Stream<Arguments> = Stream.of(
            *trueTypes.map {
                Arguments.of(it, typeOf<String>(), true)
            }.toTypedArray(),
            *trueTypes.map {
                Arguments.of(typeOf<String>(), it, true)
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
            Arguments.of("sourceIntList", typeOf<List<Int>>(), typeOf<String>(), false, "", "sourceIntList.joinToString()"),
            Arguments.of("sourceIntList", typeOf<List<Int>>(), typeOf<String>(), false, "-", "sourceIntList.joinToString(\"-\")"),
            Arguments.of("nullableSourceIntList", typeOf<List<Int>?>(), typeOf<String>(), false, "", "nullableSourceIntList!!.joinToString()"),
            Arguments.of("nullableSourceIntList", typeOf<List<Int>?>(), typeOf<String>(), true, "", "nullableSourceIntList?.joinToString()"),
            Arguments.of("nullableSourceIntList", typeOf<List<Int>?>(), typeOf<String>(), true, "/*/", "nullableSourceIntList?.joinToString(\"/*/\")"),

            Arguments.of("sourceString", typeOf<String>(), typeOf<List<Int>>(), false, "", "sourceString.split(\", \")"),
            Arguments.of("sourceString", typeOf<String>(), typeOf<List<Int>>(), false, "//", "sourceString.split(\"//\")"),
            Arguments.of("nullableSourceString", typeOf<String?>(), typeOf<List<Int>>(), false, "", "nullableSourceString!!.split(\", \")"),
            Arguments.of("nullableSourceString", typeOf<String?>(), typeOf<Set<Int>>(), false, "", "nullableSourceString!!.split(\", \").toSet()"),
            Arguments.of("nullableSourceString", typeOf<String?>(), typeOf<MutableList<Int>?>(), true, "", "nullableSourceString?.split(\", \")?.toMutableList()"),
        )
    }
}