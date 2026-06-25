package com.ucasoft.komm.website.pages.annotations

import com.ucasoft.komm.website.components.code.CodeData
import com.ucasoft.komm.website.components.code.Type
import com.ucasoft.komm.website.data.DetailItem
import com.ucasoft.komm.website.data.Step
import com.ucasoft.wrappers.lucide.Settings
import react.create

val mapFunction = DetailItem(
    Settings.create(),
    "@MapFunction",
    "Calls a configured top-level extension function for property conversion.",
    listOf(
        Step(
            "Function declaration",
            "Declare the extension function that KOMM should call for the custom property conversion.",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    fun ByteArray.toImageBitmap(): ImageBitmap = //...
                """.trimIndent()
                )
            )
        ),
        Step(
            "Classes declaration",
            "Annotate the target property with the package and optional name of the conversion function.",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    data class SourceObject(
                        val logo: ByteArray?
                    )

                    @KOMMMap(from = [SourceObject::class])
                    data class DestinationObject(
                        @MapFunction(packageName = "com.test.converters")
                        val logo: ImageBitmap?
                    )
                """.trimIndent()
                )
            )
        ),
        Step(
            "Classes declaration",
            "Specify the function name explicitly when it should not be inferred from the destination type.",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    @MapFunction(
                        packageName = "com.test.converters",
                        name = "toImageBitmap"
                    )
                """.trimIndent()
                )
            )
        ),
        Step(
            "Generated extension function",
            "The generated mapper imports and calls the configured extension function.",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    import com.test.converters.toImageBitmap

                    fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
                        logo = logo?.toImageBitmap()
                    )
                """.trimIndent()
                )
            )
        )
    )
)
