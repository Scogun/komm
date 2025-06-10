package com.ucasoft.komm.website.pages.plugins

import com.ucasoft.komm.website.components.code.CodeData
import com.ucasoft.komm.website.components.code.Type
import com.ucasoft.komm.website.data.DetailItem
import com.ucasoft.komm.website.data.Step
import com.ucasoft.wrappers.lucide.ListTree
import react.create

val iterablePlugin = DetailItem(
    ListTree.create(),
    "Iterable Plugin",
    "Supports mapping collections (like List, Set) with different types of elements, simplifying list transformations.",
    listOf(
        Step(
            "Installation",
            "Add with Gradle",
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

                dependencies {
                    implementation("com.ucasoft.komm:komm-annotations:${'$'}kommVersion")
                    ksp("com.ucasoft.komm:komm-processor:${'$'}kommVersion")
                    ksp("com.ucasoft.komm:komm-plugins-iterable:${'$'}kommVersion")
                }
            """.trimIndent()
                )
            )
        ),
        Step(
            "Allow NotNullAssertion",
            "Classes declaration",
            listOf(
                CodeData(Type.KMP, """
                    class SourceObject {
                        val intList: List<Int>? = listOf(1, 2, 3)
                    }

                    @KOMMMap(from = [SourceObject::class], config = MapConfiguration(allowNotNullAssertion = true))
                    data class DestinationObject(
                        @MapName("intList")
                        val stringList: MutableList<String>
                    )
                """.trimIndent())
            )
        ),
        Step(
            "Allow NotNullAssertion",
            "Generated extension function",
            listOf(
                CodeData(Type.KMP, """
                    public fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
                        stringList = intList!!.map { it.toString() }.toMutableList()
                    )
                """.trimIndent())
            )
        ),
        Step(
            "NullSubstitute",
            "Classes declaration",
            listOf(
                CodeData(Type.KMP, """
                    class SourceObject {
                        val intList: List<Int>? = listOf(1, 2, 3)
                    }
                    
                    @KOMMMap(from = [SourceObject::class])
                    data class DestinationObject(
                        @NullSubstitute(MapDefault(StringListResolver::class), "intList")
                        val stringList: MutableList<String>
                    )
                """.trimIndent())
            )
        ),
        Step(
            "NullSubstitute",
            "Generated extension function",
            listOf(
                CodeData(Type.KMP, """
                    public fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
                        stringList = intList?.map { it.toString() }?.toMutableList() ?: StringListResolver(null).resolve()
                    )
                """.trimIndent())
            )
        )
    )
)