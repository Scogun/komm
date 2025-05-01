package com.ucasoft.komm.processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.ucasoft.komm.abstractions.KOMMConverter

open class SatelliteTests : CompilationTests() {


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