package com.ucasoft.komm.plugins.enum

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class EnumPluginTests {

    private val plugin = EnumPlugin()

    @ParameterizedTest
    @MethodSource("forCastResultArguments")
    fun forCastResult(sourceKind: ClassKind, destinationKind: ClassKind, result: Boolean) {
        plugin.forCast(buildKSType(sourceKind), buildKSType(destinationKind)).shouldBe(result)
    }

    @Test
    fun forCastResultIsFalseForTheSameEnum() {
        plugin.forCast(buildKSType(ClassKind.ENUM_CLASS), buildKSType(ClassKind.ENUM_CLASS, true)).shouldBe(false)
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

    companion object {

        @JvmStatic
        fun forCastResultArguments(): Stream<Arguments> = Stream.of(
            Arguments.of(ClassKind.ENUM_CLASS, ClassKind.ENUM_CLASS, true),
            Arguments.of(ClassKind.ANNOTATION_CLASS, ClassKind.ENUM_CLASS, false),
            Arguments.of(ClassKind.ENUM_CLASS, ClassKind.CLASS, false),
            Arguments.of(ClassKind.INTERFACE, ClassKind.ENUM_CLASS, false)
        )
    }
}