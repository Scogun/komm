package com.ucasoft.komm.gradle

import org.gradle.api.Project

abstract class KOMMPluginExtension(private val project: Project) {

    private val pluginConfig = KOMMPluginConfig()

    internal val plugins : Set<String>
        get() = pluginConfig.plugins

    fun plugins(configure: KOMMPluginConfig.() -> Unit) {
        pluginConfig.configure()
    }

    internal fun apply(configuration: String = "ksp") {
        pluginConfig.plugins.forEach {
            println("Adding $it plugin for $configuration")
            project.dependencies.add(
                configuration,
                "com.ucasoft.komm:komm-plugins-$it:0.22.8"
            )
        }
    }
}