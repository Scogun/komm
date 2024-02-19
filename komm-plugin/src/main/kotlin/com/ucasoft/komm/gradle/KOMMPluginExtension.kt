package com.ucasoft.komm.gradle

import org.gradle.api.Action
import org.gradle.api.Project

abstract class KOMMPluginExtension(private val project: Project) {

    fun apply(action: Action<KOMMPluginConfig>) {
        val config = KOMMPluginConfig()
        action.execute(config)
        apply(config)
    }

    fun apply(config: KOMMPluginConfig) {

    }
}