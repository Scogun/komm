package com.ucasoft.komm.plugins

import com.google.devtools.ksp.symbol.KSType
import kotlin.reflect.KClass

interface KOMMTypePlugin: KOMMCastPlugin {

    fun forType(sourceType: KSType): Boolean

    fun sourceType(sourceType: KSType): KClass<*>
}