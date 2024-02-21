package com.ucasoft.komm.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class KOMMPlugin: Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create("komm", KOMMPluginExtension::class.java, project)
    }
}