package com.ucasoft.komm.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.writeTo
import com.ucasoft.komm.annotations.KOMMMap
import com.ucasoft.komm.plugin.KOMMPlugin
import io.github.classgraph.ClassGraph

class KOMMSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {

        loadPlugins()

        val symbols =
            resolver.getSymbolsWithAnnotation(KOMMMap::class.qualifiedName!!).filterIsInstance<KSClassDeclaration>()

        if (!symbols.iterator().hasNext()) {
            return emptyList()
        }


        symbols.groupBy { it.packageName }.forEach { (pn, cs) ->
            val functions = mutableListOf<FunSpec>()

            cs.forEach {
                it.accept(KOMMVisitor(functions), Unit)
            }

            val file = FileSpec.builder(pn.asString(), "MappingExtensions").apply { functions.forEach { this.addFunction(it) } }.build()

            file.writeTo(codeGenerator, false)
        }

        return symbols.filterNot { it.validate() }.toList()
    }

    private fun loadPlugins() {
        val plugins = mutableListOf<KOMMPlugin>()
        ClassGraph().enableClassInfo().scan().use {
            it.getSubclasses(KOMMPlugin::class.java.name).loadClasses().forEach { plugins.add(it.getDeclaredConstructor().newInstance() as KOMMPlugin) }
        }

        plugins.forEach {
            logger.error(it.process())
        }
    }
}