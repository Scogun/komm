package com.ucasoft.komm.processor.extensions

import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter

fun KSTypeArgument?.resolveTypeArgument(typeSubstitutions: Map<String, KSType>): KSType? {
    val type = this?.type?.resolve() ?: return null
    val typeParameter = type.declaration as? KSTypeParameter ?: return type
    return typeSubstitutions[typeParameter.name.asString()]
}
