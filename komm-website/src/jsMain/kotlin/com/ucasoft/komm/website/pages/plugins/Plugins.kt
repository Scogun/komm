package com.ucasoft.komm.website.pages.plugins

import com.ucasoft.komm.website.data.DetailItem
import com.ucasoft.komm.website.components.code.CodeData
import com.ucasoft.komm.website.data.ListPathItem
import com.ucasoft.komm.website.pages.ListPage
import com.ucasoft.komm.website.components.code.Type
import com.ucasoft.komm.website.data.Step
import com.ucasoft.wrappers.lucide.Database
import com.ucasoft.wrappers.lucide.ListTree
import com.ucasoft.wrappers.lucide.Puzzle
import react.FC
import react.create
import react.router.useLoaderData


val pluginData = listOf(
    DetailItem(
        ListTree.create(),
        "Iterable Plugin",
        "Supports mapping collections (like List, Set) with different types of elements, simplifying list transformations.",
        listOf(
            Step( "Installation",
                "",
                listOf(
                    CodeData(
                        Type.KMP, """
                plugins {
                    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
                }

                val kommVersion = "0.25.0"

                //...

                dependencies {
                    add("kspJvm", "com.ucasoft.komm:komm-plugins-iterable:${'$'}kommVersion")
                    add("kspJvm", "com.ucasoft.komm:komm-processor:${'$'}kommVersion")
                    add("kspJs", "com.ucasoft.komm:komm-plugins-iterable:${'$'}kommVersion")
                    add("kspJs", "com.ucasoft.komm:komm-processor:${'$'}kommVersion")
                    // Add other platforms like `kspAndroidNativeX64`, `kspLinuxX64`, `kspMingwX64` etc.
                }
            """.trimIndent()
                    ),
                    CodeData(
                        Type.JVM, """
                plugins {
                    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
                }

                val kommVersion = "0.25.0"

                depensencies {
                    implementation("com.ucasoft.komm:komm-annotations:${'$'}kommVersion")
                    ksp("com.ucasoft.komm:komm-processor:${'$'}kommVersion")
                    ksp("com.ucasoft.komm:komm-plugins-iterable:${'$'}kommVersion")
                }
            """.trimIndent()
                    )
                )
            ))),
    DetailItem(
        Database.create(),
        "Exposed Plugin",
        "Provides mapping from Exposed Table Objects (ResultRow) to your data classes for easy database interaction.",
        listOf(
            Step( "Installation",
                "",
                listOf(
            CodeData(Type.JVM, """
                plugins {
                    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
                }

                val kommVersion = "0.25.0"

                depensencies {
                    implementation("com.ucasoft.komm:komm-annotations:${'$'}kommVersion")
                    ksp("com.ucasoft.komm:komm-processor:${'$'}kommVersion")
                    ksp("com.ucasoft.komm:komm-plugins-exposed:${'$'}kommVersion")
                }
            """.trimIndent())
        )))),
    DetailItem(
        Puzzle.create(),
        "Enum Plugin",
        "Supports mapping enums from other enums, including default value annotations for robustness.",
        listOf(
            Step( "Installation",
                "",
                listOf(
            CodeData(
                Type.KMP, """
                plugins {
                    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
                }

                val kommVersion = "0.25.0"

                //...

                dependencies {
                    add("kspJvm", "com.ucasoft.komm:komm-plugins-enum:${'$'}kommVersion")
                    add("kspJvm", "com.ucasoft.komm:komm-processor:${'$'}kommVersion")
                    add("kspJs", "com.ucasoft.komm:komm-plugins-enum:${'$'}kommVersion")
                    add("kspJs", "com.ucasoft.komm:komm-processor:${'$'}kommVersion")
                    // Add other platforms like `kspAndroidNativeX64`, `kspLinuxX64`, `kspMingwX64` etc.
                }
            """.trimIndent()),
            CodeData(Type.JVM, """
                plugins {
                    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
                }

                val kommVersion = "0.25.0"

                depensencies {
                    implementation("com.ucasoft.komm:komm-annotations:${'$'}kommVersion")
                    ksp("com.ucasoft.komm:komm-processor:${'$'}kommVersion")
                    ksp("com.ucasoft.komm:komm-plugins-enum:${'$'}kommVersion")
                }
            """.trimIndent())
        )
    ))))

val Plugins = FC {

    val data = useLoaderData().unsafeCast<ListPathItem>()

    ListPage {
        icon = data.icon
        title = data.title
        items = data.listItems
    }
}