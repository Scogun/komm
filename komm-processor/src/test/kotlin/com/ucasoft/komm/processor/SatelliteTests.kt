package com.ucasoft.komm.processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.ucasoft.komm.abstractions.KOMMContextConverter
import com.ucasoft.komm.abstractions.KOMMContextResolver
import com.ucasoft.komm.abstractions.KOMMConverter
import com.ucasoft.komm.abstractions.KOMMResolver

open class SatelliteTests : CompilationTests() {

    protected fun buildResolver(destinationType: ClassName, destType: ClassName, statement: String) = buildSatellite(
        "TestResolver",
        KOMMResolver::class.asTypeName().parameterizedBy(destinationType, destType),
        "destination",
        destinationType.copy(true),
        "resolve",
        null,
        destType,
        statement
    )

    protected fun buildConverter(
        sourceType: ClassName,
        srcType: ClassName,
        destinationType: ClassName,
        destType: ClassName,
        statement: String
    ) =
        buildSatellite(
            "TestConverter",
            KOMMConverter::class.asTypeName().parameterizedBy(sourceType, srcType, destinationType, destType),
            "source",
            sourceType,
            "convert",
            srcType,
            destType,
            statement
        )

    protected fun buildContextConverter(
        sourceType: ClassName,
        srcType: ClassName,
        contextType: ClassName,
        destinationType: ClassName,
        destType: ClassName,
        statement: String
    ) =
        buildContextSatellite(
            "TestContextConverter",
            KOMMContextConverter::class.asTypeName().parameterizedBy(
                sourceType,
                srcType,
                contextType,
                destinationType,
                destType
            ),
            "source",
            sourceType,
            contextType,
            "convert",
            srcType,
            destType,
            statement
        )

    protected fun buildContextResolver(
        destinationType: ClassName,
        contextType: ClassName,
        destType: ClassName,
        statement: String
    ) =
        buildContextSatellite(
            "TestContextResolver",
            KOMMContextResolver::class.asTypeName().parameterizedBy(contextType, destinationType, destType),
            "destination",
            destinationType.copy(true),
            contextType,
            "resolve",
            null,
            destType,
            statement
        )

    protected fun buildContextSatellite(
        className: String,
        supperClass: ParameterizedTypeName,
        firstParameterName: String,
        firstParameterType: TypeName,
        contextType: TypeName,
        overrideFunctionName: String,
        memberType: ClassName?,
        returnType: TypeName,
        statement: String
    ) = FileSpec
        .builder(packageName, "$className.kt")
        .addType(
            TypeSpec
                .classBuilder(className)
                .superclass(supperClass)
                .addSuperclassConstructorParameter(firstParameterName)
                .addSuperclassConstructorParameter("context")
                .primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .addParameter(firstParameterName, firstParameterType)
                        .addParameter("context", contextType)
                        .build()
                )
                .addFunction(
                    FunSpec
                        .builder(overrideFunctionName)
                        .addModifiers(KModifier.OVERRIDE)
                        .apply {
                            if (memberType != null) {
                                addParameter("sourceMember", memberType)
                            }
                        }
                        .returns(returnType)
                        .addStatement(statement)
                        .build()
                )
                .build()
        )
        .build()

    protected fun buildSatellite(
        className: String,
        supperClass: ParameterizedTypeName,
        sourceName: String,
        sourceType: TypeName,
        overrideFunctionName: String,
        memberType: ClassName?,
        returnType: TypeName,
        statement: String
    ) = FileSpec
        .builder(packageName, "$className.kt")
        .addImport("kotlin.math", "roundToInt")
        .addType(
            TypeSpec
                .classBuilder(className)
                .superclass(supperClass)
                .addSuperclassConstructorParameter(sourceName)
                .primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .addParameter(sourceName, sourceType)
                        .build()
                )
                .addFunction(
                    FunSpec
                        .builder(overrideFunctionName)
                        .addModifiers(KModifier.OVERRIDE)
                        .apply {
                            if (memberType != null) {
                                addParameter("${sourceName}Member", memberType)
                            }
                        }
                        .returns(returnType)
                        .addStatement(statement)
                        .build()
                )
                .build()
        )
        .build()
}
