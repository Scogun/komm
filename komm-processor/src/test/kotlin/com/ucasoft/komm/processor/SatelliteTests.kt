package com.ucasoft.komm.processor

import com.squareup.kotlinpoet.*

open class SatelliteTests : CompilationTests() {

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