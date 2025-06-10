package com.ucasoft.komm.website.pages.annotations

import com.ucasoft.komm.website.components.code.CodeData
import com.ucasoft.komm.website.components.code.Type
import com.ucasoft.komm.website.data.DetailItem
import com.ucasoft.komm.website.data.Step
import com.ucasoft.wrappers.lucide.Settings
import react.create

val nullSubstitute = DetailItem(
    Settings.create(), "@NullSubstitute", "Extends mapping from nullable type properties", listOf(
        Step(
            "Map From",
            "Classes declaration",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    @KOMMMap(
                        from = [SourceObject::class]
                    )
                    data class DestinationObject(
                        @NullSubstitute(MapDefault(IntResolver::class))
                        val id: Int
                    ) {
                        @NullSubstitute(MapDefault(IntResolver::class), "id")
                        var otherId: Int = 0
                    }
                """.trimIndent()
                )
            )
        ),
        Step(
            "Map From",
            "Generated extension function",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
                        id = id ?: IntResolver(null).resolve()
                    ).also {
                        it.otherId = id ?: IntResolver(it).resolve()
                    }
                """.trimIndent()
                )
            )
        ),
        Step(
            "Map To",
            "Classes declaration",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    @KOMMMap(
                        to = [DestinationObject::class]
                    )
                    data class SourceObject(
                        @NullSubstitute(MapDefault(IntResolver::class))
                        val id: Int?
                    ) 
                """.trimIndent()
                )
            )
        ),
        Step(
            "Map To",
            "Generated extension function",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
                        id = id ?: IntResolver(null).resolve()
                    )
                """.trimIndent()
                )
            )
        )
    )
)