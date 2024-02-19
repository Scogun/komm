package com.ucasoft.komm.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class KOMMPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        target.extensions.create("komm", KOMMPluginExtension::class.java, target)
    }
}