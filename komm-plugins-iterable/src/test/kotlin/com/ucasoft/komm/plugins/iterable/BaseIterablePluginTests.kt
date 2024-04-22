package com.ucasoft.komm.plugins.iterable

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.MUTABLE_LIST
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.ucasoft.komm.annotations.NullSubstitute
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.BeforeAll
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.allSuperclasses

open class BaseIterablePluginTests {

    protected fun buildKSType(kType: KType) = with(mockk<KSType>()) {
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
        every { this@with.toTypeName() } answers {
            WildcardTypeName.producerOf(kType.classifier as KClass<*>).copy(nullable = kType.isMarkedNullable)
        }
        this
    }

    protected fun buildDestination(hasNullSubstitute: Boolean): KSPropertyDeclaration {
        val destinationProperty = with(mockk<KSPropertyDeclaration>()) {
            every { annotations } returns if (hasNullSubstitute) listOf(with(mockk<KSAnnotation>()) {
                every { shortName.asString() } returns NullSubstitute::class.simpleName.toString()
                this
            }).asSequence() else emptySequence()
            this
        }
        return destinationProperty
    }

    companion object {
        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            mockkStatic(KSClassDeclaration::getAllSuperTypes)
            mockkStatic(KSType::toClassName)
        }
    }
}