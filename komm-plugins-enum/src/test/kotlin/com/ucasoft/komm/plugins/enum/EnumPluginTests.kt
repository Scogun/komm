package com.ucasoft.komm.plugins.enum

import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.ucasoft.komm.annotations.NullSubstitute
import com.ucasoft.komm.plugins.enum.annotations.KOMMEnum
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

internal class EnumPluginTests {

    private val plugin = EnumPlugin()

    @ParameterizedTest
    @MethodSource("forCastResultArguments")
    fun forCastResult(sourceKind: ClassKind, destinationKind: ClassKind, isAssignableFrom: Boolean, result: Boolean) {
        plugin.forCast(buildKSType(sourceKind), buildKSType(destinationKind, isAssignableFrom)).shouldBe(result)
    }

    @Test
    fun forCastResultIsFalseForTheSameEnum() {
        plugin.forCast(buildKSType(ClassKind.ENUM_CLASS), buildKSType(ClassKind.ENUM_CLASS, true)).shouldBe(false)
    }

    @ParameterizedTest
    @MethodSource("castResultArguments")
    fun castResult(
        sourceName: String,
        sourceType: KType,
        sourceHasNullSubstitute: Boolean,
        destinationType: KType,
        destinationHasNullSubstitute: Boolean,
        enumDefault: String,
        result: String
    ) {
        plugin.cast(
            buildProperty(sourceHasNullSubstitute, enumDefault),
            sourceName,
            buildKSType(sourceType),
            buildProperty(destinationHasNullSubstitute, enumDefault),
            buildKSType(destinationType)
        ).shouldBe(result)
    }

    private fun buildKSType(kind: ClassKind, isAssignableFrom: Boolean = false) = with(mockk<KSType>()) {
        val type = this
        every { declaration } returns with(mockk<KSClassDeclaration>()) {
            every { classKind } returns kind
            every { isAssignableFrom(any()) } returns isAssignableFrom
            every { makeNotNullable() } returns type
            this
        }
        this
    }

    private fun buildKSType(kType: KType) = with(mockk<KSType>()) {
        every { declaration } returns with(mockk<KSClassDeclaration>()) {
            this
        }
        every { this@with.toClassName() } returns (kType.classifier as KClass<*>).asClassName()
        every { this@with.toTypeName() } answers {
            WildcardTypeName.producerOf(kType.classifier as KClass<*>).copy(nullable = kType.isMarkedNullable)
        }
        this
    }

    private fun buildProperty(hasNullSubstitute: Boolean, enumDefault: String): KSPropertyDeclaration {
        val destinationProperty = with(mockk<KSPropertyDeclaration>()) {
            every { annotations } returns when {
                hasNullSubstitute || enumDefault.isNotBlank() -> mutableListOf<KSAnnotation>().apply {
                    if (hasNullSubstitute) {
                        this.add(
                            with(mockk<KSAnnotation>()) {
                                every { shortName.asString() } returns NullSubstitute::class.simpleName.toString()
                                this
                            }
                        )
                    }
                    if (enumDefault.isNotBlank()) {
                        this.add(
                            with(mockk<KSAnnotation>()) {
                                every { shortName.asString() } returns KOMMEnum::class.simpleName.toString()
                                every { arguments } returns listOf(with(mockk<KSValueArgument>()) {
                                    every { name?.asString() } returns KOMMEnum::default.name
                                    every { value } returns enumDefault
                                    this
                                })
                                this
                            }
                        )
                    }
                }.asSequence()

                else -> emptySequence()
            }
            this
        }
        return destinationProperty
    }

    enum class FirstEnum {
        PING,
        PONG
    }

    enum class SecondEnum {
        PING,
        PONG,
        OTHER
    }

    companion object {

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            mockkStatic(KSType::toClassName)
        }

        @JvmStatic
        fun forCastResultArguments(): Stream<Arguments> = Stream.of(
            Arguments.of(ClassKind.ENUM_CLASS, ClassKind.ENUM_CLASS, false, true),
            Arguments.of(ClassKind.ENUM_CLASS, ClassKind.ENUM_CLASS, true, false),
            Arguments.of(ClassKind.ANNOTATION_CLASS, ClassKind.ENUM_CLASS, false, false),
            Arguments.of(ClassKind.ENUM_CLASS, ClassKind.CLASS, false, false),
            Arguments.of(ClassKind.INTERFACE, ClassKind.ENUM_CLASS, false, false)
        )

        @JvmStatic
        fun castResultArguments(): Stream<Arguments> = Stream.of(
            Arguments.of(
                "play",
                typeOf<FirstEnum>(),
                false,
                typeOf<SecondEnum>(),
                false,
                "",
                "EnumPluginTests.SecondEnum.valueOf(play.name)"
            ),
            Arguments.of(
                "play",
                typeOf<FirstEnum?>(),
                false,
                typeOf<SecondEnum>(),
                false,
                "",
                "EnumPluginTests.SecondEnum.valueOf(play!!.name)"
            ),
            Arguments.of(
                "play",
                typeOf<FirstEnum?>(),
                false,
                typeOf<SecondEnum>(),
                true,
                "",
                "(if (play != null) EnumPluginTests.SecondEnum.valueOf(play.name) else null)"
            ),
            Arguments.of(
                "play",
                typeOf<FirstEnum?>(),
                false,
                typeOf<SecondEnum?>(),
                false,
                "",
                "(if (play != null) EnumPluginTests.SecondEnum.valueOf(play.name) else null)"
            ),
            Arguments.of(
                "play",
                typeOf<FirstEnum>(),
                false,
                typeOf<SecondEnum>(),
                false,
                SecondEnum.OTHER.name,
                "EnumPluginTests.SecondEnum.valueOf(if (EnumPluginTests.SecondEnum.entries.any { it.name == play.name }) play.name else \"OTHER\")"
            ),
            Arguments.of(
                "play",
                typeOf<FirstEnum?>(),
                false,
                typeOf<SecondEnum>(),
                false,
                SecondEnum.OTHER.name,
                "EnumPluginTests.SecondEnum.valueOf(if (EnumPluginTests.SecondEnum.entries.any { it.name == play!!.name }) play!!.name else \"OTHER\")"
            ),
            Arguments.of(
                "play",
                typeOf<FirstEnum?>(),
                false,
                typeOf<SecondEnum>(),
                true,
                SecondEnum.OTHER.name,
                "(if (play != null) EnumPluginTests.SecondEnum.valueOf(if (EnumPluginTests.SecondEnum.entries.any { it.name == play.name }) play.name else \"OTHER\") else null)"
            )
        )
    }
}