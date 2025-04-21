package com.ucasoft.komm.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.internal.extensions.stdlib.capitalized
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMetadataTarget

class KOMMPlugin: Plugin<Project> {

    lateinit var extension: KOMMPluginExtension

    override fun apply(project: Project) {
        project.pluginManager.apply("com.google.devtools.ksp")
        extension = project.extensions.create("komm", KOMMPluginExtension::class.java, project)

        val isKotlinMultiplatform = project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")
        val isKotlinJvm = project.plugins.hasPlugin("org.jetbrains.kotlin.jvm")
        when {
            isKotlinMultiplatform -> configureForMultiplatform(project)
            isKotlinJvm -> configureForJvm(project)
            else -> project.logger.warn("Komm plugin supports only JVM or Multiplatform projects")
        }
    }

    private fun configureForMultiplatform(project: Project) {
        project.afterEvaluate {
            val multiplatformExtension = project.extensions.findByType(KotlinMultiplatformExtension::class.java)
            multiplatformExtension?.targets?.forEach {
                if (it !is KotlinMetadataTarget) {
                    val kspDependencies = "ksp${it.name.capitalized()}"
                    println("Adding KSP dependency $kspDependencies...")
                    project.dependencies.add(kspDependencies, "com.ucasoft.komm:komm-processor:0.22.8")
                    extension.apply(kspDependencies)
                }
            }
            multiplatformExtension?.sourceSets?.maybeCreate("commonMain").also {
                it?.dependencies {
                    implementation("com.ucasoft.komm:komm-annotations:0.22.8")
                }
            }
        }
    }

    private fun configureForJvm(project: Project) {
        project.dependencies.add("ksp", "com.ucasoft.komm:komm-processor:0.22.8")
        extension.apply()
    }
}