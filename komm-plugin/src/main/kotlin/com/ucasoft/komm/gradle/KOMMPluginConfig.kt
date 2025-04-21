package com.ucasoft.komm.gradle

class KOMMPluginConfig {
    internal val plugins = mutableSetOf<String>()

    fun include(vararg name: String) {
        plugins.addAll(name)
    }
}