package com.ucasoft.komm.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class KOMMPlugin: Plugin<Project> {

    fun apply(project: Project) {
        project.Extensions.create("komm", KOMMPluginExtension::class.java)
    }
}