package com.ucasoft.komm.website.pages.annotations

import com.ucasoft.komm.website.components.code.CodeData
import com.ucasoft.komm.website.components.code.Type
import com.ucasoft.komm.website.data.DetailItem
import com.ucasoft.komm.website.data.Step
import com.ucasoft.wrappers.lucide.Settings
import react.create

val mapResolve =
    DetailItem(
        Settings.create(), "@MapDefault", "Provides possibility to add default values for orphans properties", listOf(
            Step(
                "Use Resolver",
                "Resolver declaration",
                listOf(
                    CodeData(
                        Type.KMP,
                        """
                            class DateResolver(destination: DestinationObject?) : KOMMResolver<DestinationObject, Date>(destination) {

                                override fun resolve(): Date = Date.from(Instant.now())
                            }
                        """.trimIndent()
                    )
                )
            ),
            Step(
                "Use Resolver",
                "Classes declaration",
                listOf(
                    CodeData(
                        Type.KMP,
                        """
                            @KOMMMap(from = [SourceObject::class])
                            data class DestinationObject(
                                //...
                                @MapDefault<DateResolver>(DateResolver::class)
                                val activeDate: Date
                            ) {
                                //...
                                @MapDefault<DateResolver>(DateResolver::class)
                                var otherDate: Date = Date.from(Instant.now())
                            }
                        """.trimIndent()
                    )
                )
            ),
            Step(
                "Use Resolver",
                "Generated extension function",
                listOf(
                    CodeData(
                        Type.KMP,
                        """
                            fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
                                //...
                                activeDate = DateResolver(null).resolve()
                            ).also { 
                                //...
                                it.otherDate = DateResolver(it).resolve()
                            }
                        """.trimIndent()
                    )
                )
            )
        )
    )