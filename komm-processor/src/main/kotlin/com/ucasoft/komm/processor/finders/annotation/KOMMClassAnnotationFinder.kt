package com.ucasoft.komm.processor.finders.annotation

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.ucasoft.komm.annotations.MapEmbedded
import com.ucasoft.komm.annotations.MapTargetDefault

class KOMMClassAnnotationFinder(
    forClass: KSType,
    private val annotationOwner: KSClassDeclaration
) : KOMMAnnotationFinder(forClass) {

    fun findTargetDefaultResolver(member: KSPropertyDeclaration): KSType? {
        val annotations = annotationOwner.annotations
            .filter { it.shortName.asString() == MapTargetDefault::class.simpleName }
            .filter {
                it.arguments
                    .first { argument -> argument.name?.asString() == MapTargetDefault::name.name }
                    .value
                    .toString() == member.simpleName.asString()
            }
            .associateWith(::associateWithFor)

        val annotation = filterAnnotationsByClass(annotations, member.simpleName.asString()) ?: return null
        val defaultArgument =
            annotation.arguments.first { it.name?.asString() == MapTargetDefault::default.name }.value as KSAnnotation
        return getMapDefaultResolver(defaultArgument)
    }

    fun getSuitedEmbeddedAnnotations(): List<KSAnnotation> =
        annotationOwner.annotations
            .filter { it.shortName.asString() == MapEmbedded::class.simpleName }
            .associateWith(::associateWithFor)
            .filter { it.value.isEmpty() || it.value.contains(forClassName) }
            .keys
            .toList()
}