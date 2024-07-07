package com.ucasoft.komm.plugins.exposed

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.jetbrains.exposed.sql.Table
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class ExposedPluginTests {

    private val plugin = ExposedPlugin()

    @ParameterizedTest
    @MethodSource("forTypeResultArguments")
    fun forTypeResult(sourceClass: ClassName, result: Boolean) {
        val goodSourceType = with(mockk<KSType>()) {
            every { declaration } returns with(mockk<KSClassDeclaration>()) {
                every { superTypes } returns listOf(
                    with(mockk<KSTypeReference>()) {
                        every { this@with.toTypeName() } returns sourceClass
                        this
                    }
                ).asSequence()
                this
            }
            this
        }
        plugin.forType(goodSourceType).shouldBe(result)
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
    }
}