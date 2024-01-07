package com.ucasoft.komm.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import com.ucasoft.komm.annotations.KOMMMap

class KOMMSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(KOMMMap::class.qualifiedName!!).filterIsInstance<KSClassDeclaration>()

        if (!symbols.iterator().hasNext()) {
            return emptyList()
        }

        val functions = mutableListOf<FunSpec>()

        symbols.forEach { it.accept(Visitor(functions), Unit) }

        var file = FileSpec.builder("com.ucasoft.komm.simple", "GeneratedFunctions")
        functions.forEach {
           file = file.addFunction(it)
        }

        file.build().writeTo(codeGenerator, false)

        return symbols.filterNot { it.validate() }.toList()
    }

    inner class Visitor(private val functions: MutableList<FunSpec>) : KSVisitorVoid() {

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val annotation = classDeclaration.annotations.first { it.shortName.asString() == KOMMMap::class.simpleName }
            val nameArgument = annotation.arguments.first { it.name?.asString() == KOMMMap::from.name }
            val sourceClasses = nameArgument.value as ArrayList<KSType>
            val fromSourceFunctionName = "to$classDeclaration"
            for (source in sourceClasses) {
                functions.add(
                    FunSpec
                        .builder(fromSourceFunctionName)
                        .receiver(source.toTypeName())
                        .returns(classDeclaration.toClassName())
                        .addStatement(buildStatement(source, classDeclaration))
                        .build()
                )
            }
        }

        private fun buildStatement(source: KSType, destination: KSClassDeclaration): String {
            val sourceProperties = (source.declaration as KSClassDeclaration).getAllProperties().associate {
                it.toString() to it.type.toString()
            }
            var result = "return $destination(\n"
            destination.getAllProperties().forEach {
                result += "$it = ${getSourceWithCast(it, sourceProperties[it.toString()])}, "
            }
            return "${result.trimEnd(',', ' ')})"
        }

        private fun getSourceWithCast(destinationProperty: KSPropertyDeclaration, sourcePropertyType: String?): String {
            val pureProperty = destinationProperty.toString()
            if (destinationProperty.type.toString() != sourcePropertyType) {
                return "$pureProperty.to${destinationProperty.type}()"
            }

            return pureProperty
        }
    }
}