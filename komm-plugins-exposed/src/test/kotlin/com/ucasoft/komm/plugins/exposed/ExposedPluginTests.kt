package com.ucasoft.komm.plugins.exposed

import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.Test

internal class ExposedPluginTests {

    private val plugin = ExposedPlugin()

    @ParameterizedTest
    @MethodSource("forTypeResultArguments")
    fun forTypeResult(sourceClass: ClassName, result: Boolean) {
        val sourceType = mockKSType(sourceClass)
        plugin.forType(sourceType).shouldBe(result)
    }

    @Test
    fun sourceTypeResult() {
        plugin.sourceType(mockk()).shouldBe(ResultRow::class)
    }

    @ParameterizedTest
    @MethodSource("forCastResultArguments")
    fun forCastResult(sourceClass: ClassName, result: Boolean) {
        val sourceType = mockKSType(sourceClass)
        plugin.forCast(sourceType, mockKSType(sourceClass)).shouldBe(result)
    }

    @ParameterizedTest
    @MethodSource("castResultArguments")
    fun castResult(columnParameter: TypeName, destinationType: TypeName, castExtension: String) {
        val sourceType = with(mockKSType(Column::class.asTypeName().parameterizedBy(columnParameter))) {
            every { arguments } returns listOf(with(mockk<KSTypeArgument>()){
                every { this@with.toTypeName() } returns columnParameter
                this
            })
            this
        }
        val sourceProperty = with(mockk<KSClassDeclaration>()) {
            every { parentDeclaration } returns with(mockk<KSClassDeclaration>()) {
                every { simpleName } returns with(mockk<KSName>()) {
                    every { asString() } returns "Source"
                    this
                }
                this
            }
            this
        }
        val destinationProperty = with(mockk<KSPropertyDeclaration>()) {
            every { type } returns with(mockk<KSTypeReference>()) {
                every { this@with.toString() } returns destinationType.toString().substring(7)
                this
            }
            this
        }
        plugin.cast(sourceProperty, "sourceName", sourceType, destinationProperty, mockKSType(destinationType)).shouldBe("this[Source.sourceName]${castExtension}")
    }

    private fun mockKSType(sourceClass: TypeName) =
        with(mockk<KSType>()) {
            every { declaration } returns with(mockk<KSClassDeclaration>()) {
                every { superTypes } returns listOf(
                    with(mockk<KSTypeReference>()) {
                        every { this@with.toTypeName() } returns sourceClass
                        this
                    }
                ).asSequence()
                this
            }
            every { this@with.toClassName() } returns ClassName.bestGuess(sourceClass.toString())
            every { this@with.toTypeName() } returns sourceClass
            this
        }

    companion object {

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            mockkStatic(KSTypeReference::toTypeName)
        }

        @JvmStatic
        fun forTypeResultArguments(): Stream<Arguments> = Stream.of(
            Arguments.of(
                Table::class.asTypeName(), true
            ),
            Arguments.of(
                Int::class.asTypeName(), false
            ),
        )

        @JvmStatic
        fun forCastResultArguments(): Stream<Arguments> = Stream.of(
            Arguments.of(
                Column::class.asTypeName(), true
            ),
            Arguments.of(
                Int::class.asTypeName(), false
            ),
        )

        @JvmStatic
        fun castResultArguments(): Stream<Arguments> = Stream.of(
            Arguments.of(
                INT,
                INT,
                ""
            ),
            Arguments.of(
                INT,
                STRING,
                ".toString()"
            ),
            Arguments.of(
                STRING,
                INT,
                ".toInt()"
            )
        )
    }
}