package com.ucasoft.komm.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.writeTo
import com.ucasoft.komm.annotations.KOMMMap
import com.ucasoft.komm.plugins.KOMMCastPlugin
import com.ucasoft.komm.plugins.KOMMPlugin
import com.ucasoft.komm.plugins.KOMMTypePlugin
import io.github.classgraph.ClassGraph
import kotlin.reflect.KClass

class KOMMSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {

        val symbols =
            resolver.getSymbolsWithAnnotation(KOMMMap::class.qualifiedName!!).filterIsInstance<KSClassDeclaration>()

        if (!symbols.iterator().hasNext()) {
            return emptyList()
        }

        val plugins = loadPlugins()

        symbols.groupBy { it.packageName }.forEach { (packageName, classDeclarations) ->
            val functions = mutableListOf<FunSpec>()
            val imports = mutableMapOf<String, List<String>>()

            classDeclarations.forEach {
                it.accept(KOMMVisitor(functions, imports, plugins), Unit)
            }

            val file = FileSpec
                .builder(packageName.asString(), "MappingExtensions")
                .apply {
                    imports.forEach { this.addImport(it.key, it.value) }
                    functions.forEach { this.addFunction(it) }
                }
                .build()

            file.writeTo(codeGenerator, false)
        }

        return symbols.filterNot { it.validate() }.toList()
    }

    private fun loadPlugins(): Map<KClass<out KOMMPlugin>, List<Class<*>>> {
        ClassGraph().enableClassInfo().scan().use {
            return it.getClassesImplementing(KOMMPlugin::class.java).filterNot { it.isAbstract }
                .map { it.interfaces.loadClasses() to it.loadClass() }.groupBy {
                    if (it.first.contains(KOMMTypePlugin::class.java)) {
                        KOMMTypePlugin::class
                    } else if (it.first.contains(KOMMCastPlugin::class.java)) {
                        KOMMCastPlugin::class
                    } else {
                        KOMMPlugin::class
                    }
                }.mapValues { it.value.map { it.second } }
        }
    }
}