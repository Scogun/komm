package com.ucasoft.komm.website.pages.annotations

import com.ucasoft.komm.website.components.code.CodeData
import com.ucasoft.komm.website.components.code.Type
import com.ucasoft.komm.website.data.DetailItem
import com.ucasoft.komm.website.data.Step
import com.ucasoft.wrappers.lucide.Settings
import react.create

val kommMap = DetailItem(
    Settings.create(), "@KOMMMap", "Core annotation to enable mapping for a class", listOf(
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
        ),
        Step(
            "Use Context",
            "Attach a context type to the mapping so the generated function requires that context argument.",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    data class TransactionMapContext(
                        val accounts: Map<Long, Account>,
                        val accountCurrencies: Map<Long, AccountCurrency>,
                        val categories: Map<Long, Category>
                    )

                    @KOMMMap(from = [DbTransaction::class], context = TransactionMapContext::class)
                    data class Transaction(
                        //...
                    )
                """.trimIndent()
                )
            )
        ),
        Step(
            "Use Context",
            "The generated mapper receives the context as a kommContext parameter.",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    fun DbTransaction.toTransaction(kommContext: TransactionMapContext): Transaction = Transaction(
                        //...
                    )
                """.trimIndent()
                )
            )
        ),
        Step(
            "Multi Sources Support",
            "Classes declaration",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    @KOMMMap(
                        from = [FirstSourceObject::class, SecondSourceObject::class]
                    )
                    data class DestinationObject(
                        @NullSubstitute(MapDefault(IntResolver::class), [FirstSourceObject::class])
                        @MapName("userId", [SecondSourceObject::class])
                        val id: Int
                    ) {
                        @NullSubstitute(MapDefault(IntResolver::class), "id", [FirstSourceObject::class])
                        var otherId: Int = 0
                    }
                    
                    data class FirstSourceObject(
                      val id: Int?
                    )
                    
                    data class SecondSourceObject(
                        val userId: Int
                    )
                """.trimIndent()
                )
            )
        ),
        Step(
            "Multi Sources Support",
            "Different sources should be configured different",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    @KOMMMap(
                        from = [FirstSourceObject::class],
                        config = MapConfiguration(
                            allowNotNullAssertion = true
                        )
                    )
                    @KOMMMap(
                        from = [SecondSourceObject::class]
                    )
                    data class DestinationObject(
                      @NullSubstitute(MapDefault(IntResolver::class), [FirstSourceObject::class])
                      @MapName("userId", [SecondSourceObject::class])
                      val id: Int
                    ) {
                      @NullSubstitute(MapDefault(IntResolver::class), "id", [FirstSourceObject::class])
                      var otherId: Int = 0
                    }
                """.trimIndent()
                )
            )
        ),
        Step(
            "Multi Sources Support",
            "Generated extension function",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    fun FirstSourceObject.toDestinationObject(): DestinationObject = DestinationObject(
                        id = id ?: IntResolver(null).resolve()
                    ).also {
                        it.otherId = id ?: IntResolver(it).resolve()
                    }
                    
                    fun SecondSourceObject.toDestinationObject(): DestinationObject = DestinationObject(
                      id = userId
                    )
                """.trimIndent()
                )
            )
        ),
        Step(
            "Disable AutoCast",
            "Classes declaration",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    @KOMMMap(
                        from = [SourceObject::class],
                        config = MapConfiguration(
                            tryAutoCast = false
                        )
                    )
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
            "Disable AutoCast",
            "Generation result",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    e: [ksp] com.ucasoft.komm.processor.exceptions.KOMMCastException: AutoCast is turned off! You have to use @MapConvert annotation to cast (stringToInt: Int) from (stringToInt: String)
                """.trimIndent(),
                    "console"
                )
            )
        ),
        Step(
            "Allow NotNullAssertion",
            "Classes declaration",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                        @KOMMMap(
                            from = [SourceObject::class],
                            config = MapConfiguration(
                              allowNotNullAssertion = true
                            )
                        )
                        data class DestinationObject(
                            val id: Int
                        )
                        data class SourceObject(
                            val id: Int?
                        )
                    """.trimIndent()
                )
            )
        ),
        Step(
            "Allow NotNullAssertion",
            "Generation result",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                        fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
                            id = id!!
                        )
                    """.trimIndent(),
                )
            )
        ),
        Step(
            "Allow NotNullAssertion",
            "Otherwise",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                        e: [ksp] com.ucasoft.komm.processor.exceptions.KOMMCastException: Auto Not-Null Assertion is not allowed! You have to use @NullSubstitute annotation for id property.
                    """.trimIndent(),
                    "console"
                )
            )
        ),
        Step(
            "Nullable Context",
            "Set nullableContext = true when a mapping context should be optional at the mapping function boundary.",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    @KOMMMap(
                        from = [SourceObject::class],
                        context = SourceMapContext::class,
                        config = MapConfiguration(
                            nullableContext = true
                        )
                    )
                    data class DestinationObject(
                        val id: Int
                    )
                """.trimIndent()
                )
            )
        ),
        Step(
            "Nullable Context",
            "KOMM generates a nullable context parameter with a default null value.",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    fun SourceObject.toDestinationObject(kommContext: SourceMapContext? = null): DestinationObject = DestinationObject(
                        id = id
                    )
                """.trimIndent()
                )
            )
        ),
        Step(
            "Change Convert Function Name",
            "Classes declaration",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    @KOMMMap(
                        from = [SourceObject::class],
                        config = MapConfiguration(
                            convertFunctionName = "convertToDestination"
                        )
                    )
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
            "Change Convert Function Name",
            "Generated extension function",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    fun SourceObject.convertToDestination(): DestinationObject = DestinationObject(
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
)
