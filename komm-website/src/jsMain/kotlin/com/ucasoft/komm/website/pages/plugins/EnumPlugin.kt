package com.ucasoft.komm.website.pages.plugins

import com.ucasoft.komm.website.components.code.CodeData
import com.ucasoft.komm.website.components.code.Type
import com.ucasoft.komm.website.data.DetailItem
import com.ucasoft.komm.website.data.Step
import com.ucasoft.wrappers.lucide.Puzzle
import react.create

val enumPlugin = DetailItem(
    Puzzle.create(),
    "Enum Plugin",
    "Supports mapping enums from other enums, including default value annotations for robustness.",
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
                    add("kspJvm", "com.ucasoft.komm:komm-plugins-enum:${'$'}kommVersion")
                    add("kspJvm", "com.ucasoft.komm:komm-processor:${'$'}kommVersion")
                    add("kspJs", "com.ucasoft.komm:komm-plugins-enum:${'$'}kommVersion")
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
                    ksp("com.ucasoft.komm:komm-plugins-enum:${'$'}kommVersion")
                }
            """.trimIndent()
                )
            )
        ),
        Step(
            "Default Usage",
            "Classes declaration",
            listOf(
                CodeData(
                    Type.KMP, """
                    enum class SourceEnum {
                        UP, 
                        DOWN,
                        LEFT,
                        RIGHT
                    }

                    data class SourceObject(
                        val direction: SourceEnum
                    )

                    @KOMMMap(from = [SourceObject::class])
                    data class DestinationObject(
                        val direction: DestinationObject.DestinationEnum
                    ) {
                        enum class DestinationEnum {
                            UP,
                            DOWN,
                            OTHER
                        }
                    }
                """.trimIndent()
                )
            )
        ),
        Step(
            "Default Usage",
            "Generated extension function",
            listOf(
                CodeData(
                    Type.KMP, """
                    fun SourceObject.toDestinationObject(): toDestinationObject = toDestinationObject(
                        direction = DestinationObject.DestinationEnum.valueOf(direction.name)
                    )
                """.trimIndent()
                )
            )
        ),
        Step(
            "NullSubstitute",
            "Classes declaration",
            listOf(
                CodeData(
                    Type.KMP, """
                    data class SourceObject(
                        val direction: SourceEnum?
                    )
                    
                    @KOMMMap(from = [SourceObject::class])
                    data class DestinationObject(
                        @NullSubstitute(MapDefault(DirectionResolver::class))
                        val direction: DestinationObject.DestinationEnum
                    )
                    
                    class DirectionResolver(destination: DestinationEnum?) : KOMMResolver<DestinationEnum, DestinationObject.DestinationEnum>(destination) {
                      override fun resolve() = DestinationObject.DestinationEnum.OTHER
                    }
                """.trimIndent()
                )
            )
        ),
        Step(
            "NullSubstitute",
            "Generated extension function",
            listOf(
                CodeData(
                    Type.KMP, """
                    fun SourceObject.toDestinationObject(): toDestinationObject = toDestinationObject(
                        direction = (if (direction != null) DestinationObject.DestinationEnum.valueOf(direction.name) else null)
                          ?: DirectionResolver(null).resolve()
                    )
                """.trimIndent()
                )
            )
        ),
        Step(
            "Default Value",
            "Classes declaration",
            listOf(
                CodeData(
                    Type.KMP, """
                    data class SourceObject(
                        val direction: SourceEnum?
                    )
                    
                    @KOMMMap(from = [SourceObject::class])
                    data class DestinationObject(
                        @NullSubstitute(MapDefault(DirectionResolver::class))
                        @KOMMEnum("OTHER")
                        val direction: DestinationObject.DestinationEnum
                    )
                    
                    class DirectionResolver(destination: DestinationEnum?) : KOMMResolver<DestinationEnum, DestinationObject.DestinationEnum>(destination) {
                      override fun resolve() = DestinationObject.DestinationEnum.OTHER
                    }
                """.trimIndent()
                )
            )
        ),
        Step(
            "Default Value",
            "Generated extension function",
            listOf(
                CodeData(
                    Type.KMP, """
                    fun SourceObject.toDestinationObject(): toDestinationObject = toDestinationObject(
                        direction = (if (direction != null) DestinationObject.DestinationEnum.valueOf(if
                        (DestinationObject.DestinationEnum.entries.any { it.name == direction.name }) direction.name else "OTHER")
                        else null) ?: DirectionResolver(null).resolve()
                    )
                """.trimIndent()
                )
            )
        )
    )
)