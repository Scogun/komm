package com.ucasoft.komm.processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.tschuchort.compiletesting.KotlinCompilation
import com.ucasoft.komm.abstractions.KOMMConverter
import com.ucasoft.komm.annotations.KOMMMap
import com.ucasoft.komm.annotations.MapConvert
import com.ucasoft.komm.annotations.MapFrom
import com.ucasoft.komm.processor.exceptions.KOMMException
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

internal class ConverterTests: CompilationTests() {

    @Test
    fun mapConvertBadNameFail() {
        val sourceSpec = buildFileSpec("SourceObject", mapOf("fromId" to PropertySpecInit(Int::class)))
        val sourceType = sourceSpec.typeSpecs.first()
        val sourceObjectClassName = ClassName(packageName, sourceType.name!!)
        val converterSpec = buildConverter(sourceObjectClassName)
        val converterClassName = converterSpec.typeSpecs.first().name
        val generated = generate(
            sourceSpec,
            converterSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    "toId" to PropertySpecInit(
                        String::class,
                        parametrizedAnnotations = listOf(
                            MapConvert::class.asTypeName().parameterizedBy(sourceObjectClassName) to mapOf(
                                "name = %S" to listOf("id"),
                                "converter = %L" to listOf("$converterClassName::class")
                            )
                        )
                    )
                ),
                listOf(KOMMMap::class to mapOf("from = %L" to listOf("$sourceObjectClassName::class")))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        generated.messages.shouldContain("${KOMMException::class.simpleName}: There is no mapping for toId property! It seems you specify bad name (id) into name support annotation (e.g. @${MapFrom::class.simpleName} etc.).")
    }

    private fun buildConverter(sourceType: ClassName) = FileSpec
        .builder(packageName, "TestConverter.kt")
        .addType(
            TypeSpec
                .classBuilder("TestConverter")
                .superclass(KOMMConverter::class.asTypeName().parameterizedBy(sourceType, INT, STRING))
                .addFunction(
                    FunSpec
                        .builder("convert")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("sourceMember", Int::class)
                        .returns(String::class)
                        .addStatement("return sourceMember.toString()")
                        .build()
                )
                .build()
        )
        .build()
}