package com.ucasoft.komm.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.ucasoft.komm.annotations.KOMMMap
import java.io.OutputStream

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

        val file = codeGenerator.createNewFile(
            Dependencies(false),
            "com.ucasoft.komm.simple",
            "GeneratedFunctions"
        )
        file.write("package com.ucasoft.komm.simple\n".toByteArray())

        symbols.forEach { it.accept(Visitor(file, logger), Unit) }

        file.close()

        return symbols.filterNot { it.validate() }.toList()
    }

    inner class Visitor(private val file: OutputStream, logger: KSPLogger) : KSVisitorVoid() {

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val className = classDeclaration.toString()
            val annotation = classDeclaration.annotations.first { it.shortName.asString() == KOMMMap::class.simpleName }
            val nameArgument = annotation.arguments.first { it.name?.asString() == KOMMMap::toName.name }
            val functionName = nameArgument.value.toString()
            file.write("\n".toByteArray())
            file.write("""
                fun $className.$functionName(): String {
                    return "Hello from generated $functionName!"
                }
            """.trimIndent().toByteArray())
        }
    }
}