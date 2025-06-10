package com.ucasoft.komm.website.pages.quickStart

import com.ucasoft.komm.website.components.code.CodeData
import com.ucasoft.komm.website.data.Step
import com.ucasoft.komm.website.components.code.Type

val steps = listOf(
    Step(
        "Add KSP Plugin",
        "First, ensure you have the KSP (Kotlin Symbol Processing) plugin configured in your `build.gradle.kts` (or `build.gradle`) file:",
        listOf(
            CodeData(
                Type.KMP, """
                plugins {
                    kotlin("multiplatform") version "2.0.21"
                    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
                }
            """.trimIndent()
            ),
            CodeData(
                Type.JVM, """
                plugins {
                    kotlin("jvm") version "2.0.21"
                    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
                }
            """.trimIndent()
            )
        )
    ),
    Step(
        "Add KOMM Dependencies",
        "Add the KOMM Annotations and KOMM processor as dependencies",
        listOf(
            CodeData(
                Type.KMP, """
                val kommVersion = "0.25.0"

                kotlin {
                    jvm()
                    js(IR) {
                        browser()
                        nodejs()
                    }
                    // Other targets...
                    sourceSets {
                        val commonMain by getting {
                            dependencies {
                                api("com.ucasoft.komm:komm-annotations:${'$'}kommVersion")
                            }
                        }
                    }
                }

                dependencies {
                    add("kspJvm", "com.ucasoft.komm:komm-processor:${'$'}kommVersion")
                    add("kspJs", "com.ucasoft.komm:komm-processor:${'$'}kommVersion")
                    // Add other platforms like `kspAndroidNativeX64`, `kspLinuxX64`, `kspMingwX64` etc.
                }
            """.trimIndent()
            ),
            CodeData(
                Type.JVM, """
                val kommVersion = "0.25.0"

                dependencies {
                    implementation("com.ucasoft.komm:komm-annotations:${'$'}kommVersion")
                    ksp("com.ucasoft.komm:komm-processor:${'$'}kommVersion")
                }
            """.trimIndent()
            )
        )
    ),
    Step(
        "Simple Mapping from source object",
        "Declaration source and destination objects",
        listOf(
            CodeData(
                Type.KMP,
                """
                        class SourceObject {

                            val id = 150

                            val intToString = 300

                            val stringToInt = "250"
                        }

                        @KOMMMap(from = [SourceObject::class])
                        data class DestinationObject(
                            val id: Int,
                            val stringToInt: Int
                        ) {
                            var intToString: String = ""
                        }
                    """.trimIndent()
            )
        )
    ),
    Step(
        "Simple Mapping to destination object",
        "Declaration source and destination objects",
        listOf(
            CodeData(
                Type.KMP,
                """
                            @KOMMMap(to = [DestinationObject::class])
                            class SourceObject {
    
                                val id = 150
    
                                val intToString = 300
    
                                val stringToInt = "250"
                            }
    
                            data class DestinationObject(
                                val id: Int,
                                val stringToInt: Int
                            ) {
                                var intToString: String = ""
                            }
                        """.trimIndent()
            )
        )
    ),
    Step(
        "Generated extension function",
        "In both cases will be generated an extension function for Source object",
        listOf(
            CodeData(
                Type.KMP, """
                    fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
                        id = id,
                        stringToInt = stringToInt.toInt()
                    ).also { 
                        it.intToString = intToString.toString()
                    }
                """.trimIndent()
            )
        )
    )
)