package com.ucasoft.komm.processor

import kotlin.reflect.KClass

open class TestProperty(val name: String, val type: KClass<*>, val value: Any)