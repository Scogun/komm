package com.ucasoft.komm.website.pages.quickStart

import com.ucasoft.komm.website.pages.BreadCrumb
import com.ucasoft.komm.website.pages.CodeData
import com.ucasoft.komm.website.pages.CodeTabs
import com.ucasoft.komm.website.pages.PageContainer
import com.ucasoft.komm.website.pages.Type
import com.ucasoft.wrappers.lucide.Rocket
import com.ucasoft.wrappers.`react-syntax-highlighter`.SyntaxHighlighter
import mui.material.Card
import mui.material.CardContent
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.create
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.h3
import react.useState
import web.cssom.Color
import web.cssom.WhiteSpace
import web.cssom.integer
import web.cssom.number
import web.cssom.px

private data class Step(val title: String, val description: String, val codes: List<CodeData>)

private val steps = listOf(
    Step(
        "Add KSP Plugin",
        "First, ensure you have the KSP (Kotlin Symbol Processing) plugin configured in your `build.gradle.kts` (or `build.gradle`) file:",
        listOf(
            CodeData(Type.KMP, """
                plugins {
                    kotlin("multiplatform") version "2.0.21"
                    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
                }
            """.trimIndent()),
            CodeData(Type.JVM, """
                plugins {
                    kotlin("jvm") version "2.0.21"
                    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
                }
            """.trimIndent())
        )),
    Step(
        "Add KOMM Dependencies",
        "Add the KOMM Annotations and KOMM processor as dependencies",
        listOf(
            CodeData(Type.KMP, """
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
            """.trimIndent()),
            CodeData(Type.JVM, """
                val kommVersion = "0.25.0"

                dependencies {
                    implementation("com.ucasoft.komm:komm-annotations:${'$'}kommVersion")
                    ksp("com.ucasoft.komm:komm-processor:${'$'}kommVersion")
                }
            """.trimIndent())
        )),
        Step (
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
        Step (
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
        Step (
            "Generated extension function",
            "In both cases will be generated an extension function for Source object",
            listOf(
                CodeData(Type.KMP, """
                    fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
                        id = id,
                        stringToInt = stringToInt.toInt()
                    ).also { 
                        it.intToString = intToString.toString()
                    }
                """.trimIndent())
            )
        )
)

val QuickStart = FC {

    var codeType by useState(steps.first().codes.first().type)

    PageContainer {
        homePath = "Home"
        breadcrumbs = listOf(BreadCrumb(Rocket.create(), "Quick Start", "/quickstart"))
        Typography {
            variant = TypographyVariant.h1
            component = h1
            gutterBottom = true
            +"Quick Start"
        }
        Typography {
            variant = TypographyVariant.h5
            sx {
                color = Color("text.primary")
                marginBottom = 4.px
                fontWeight = integer(400)
            }
            +"Get KOMM up and running in your Kotlin Multiplatform or JVM project in just a few steps."
        }
        steps.map {
            Card {
                sx {
                    marginBottom = 5.px
                }
                CardContent {
                    Typography {
                        variant = TypographyVariant.h4
                        component = h3
                        gutterBottom = true
                        +it.title
                    }
                    Typography {
                        variant = TypographyVariant.body1
                        sx {
                            marginBottom = 2.px
                            whiteSpace = WhiteSpace.preLine
                            lineHeight = number(1.7)
                        }
                        +it.description
                    }
                    if (it.codes.size > 1) {
                        CodeTabs {
                            type = codeType
                            items = it.codes
                            typeChange = { newCode -> codeType = newCode }
                        }
                    } else {
                        SyntaxHighlighter {
                            language = "kotlin"
                            +it.codes.first().code
                        }
                    }
                }
            }
        }
    }
}