package com.ucasoft.komm.website.pages.annotations

import com.ucasoft.komm.website.components.code.CodeData
import com.ucasoft.komm.website.components.code.Type
import com.ucasoft.komm.website.data.DetailItem
import com.ucasoft.komm.website.data.Step
import com.ucasoft.wrappers.lucide.Settings
import react.create

val mapName = DetailItem(Settings.create(), "@MapName", "Provides possibility to map properties with different names", listOf(
    Step(
        "Classes declaration",
        "",
        listOf(
            CodeData(
                Type.KMP,
                """
                    class SourceObject {
                        //...
                        val userName = "user"
                    }

                    @KOMMMap(from = [SourceObject::class])
                    data class DestinationObject(
                        //...
                        @MapName("userName")
                        val name: String
                    ) {
                        var intToString: String = ""
                    }
                """.trimIndent()
            )
        )
    ),
    Step(
        "Classes declaration",
        "or",
        listOf(
            CodeData(
                Type.KMP,
                """
                    @KOMMMap(to = [DestinationObject::class])
                    class SourceObject {
                        //...
                        @MapName("name")
                        val userName = "user"
                    }
                    
                    data class DestinationObject(
                        //...
                        val name: String
                    ) {
                        var intToString: String = ""
                    }
                """.trimIndent()
            )
        )
    ),
    Step(
        "Classes declaration",
        "Generated extension function",
        listOf(
            CodeData(
                Type.KMP,
                """
                    fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
                        //...
                        name = userName
                    ).also { 
                        it.intToString = intToString.toString()
                    }
                """.trimIndent()
            )
        )
    )
))