package com.ucasoft.komm.gradle

import io.kotest.matchers.string.shouldNotBeEmpty
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class KOMMPluginTests {

    @field:TempDir
    private lateinit var testProjectDir: File

    private lateinit var settingsFile: File

    private lateinit var buildFile: File

    @BeforeEach
    fun setup() {
        settingsFile = File(testProjectDir, "settings.gradle.kts")
        settingsFile.appendText("""
           rootProject.name = "test" 
        """)
        buildFile = File(testProjectDir, "build.gradle.kts")
    }

    @Test
    fun `test plugin is applied for jvm`() {
        buildFile.appendText("""
            plugins {
                kotlin("jvm") version "2.0.20"
                id("com.ucasoft.komm")
            }          
        """)

        val result = runTask(":tasks")

        result.output.shouldNotBeEmpty()
    }

    @Test
    fun `test plugin is applied for multiplatform`() {
        buildFile.appendText("""
            plugins {
                kotlin("multiplatform") version "2.0.20"
                id("com.ucasoft.komm")
            } 
            kotlin {
                jvm()
                js(IR) {
                    browser()
                }
            }
        """)

        val result = runTask(":tasks")

        result.output.shouldNotBeEmpty()
    }

    private fun runTask(task: String, params: Map<String, Any> = emptyMap(), arguments: List<String> = emptyList()): BuildResult {
        return GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(mutableListOf(task).also { it.addAll(params.map { p -> "-P${p.key}=${p.value}" }) }
                .also { it.addAll(arguments) }
                .also { it.add("--stacktrace") })
            .withPluginClasspath()
            .build()
    }
}