package com.ucasoft.komm.website.pages.annotations

import com.ucasoft.komm.website.components.code.CodeData
import com.ucasoft.komm.website.components.code.Type
import com.ucasoft.komm.website.data.DetailItem
import com.ucasoft.komm.website.data.Step
import com.ucasoft.wrappers.lucide.Settings
import react.create

val mapConvert =
    DetailItem(
        Settings.create(), "@MapConvert", "Provides possibility to add additional logic for properties mapping", listOf(
            Step(
                "Use Converter",
                "Converter declaration",
                listOf(
                    CodeData(
                        Type.KMP,
                        """
                            class CostConverter(source: SourceObject) : KOMMConverter<SourceObject, Double, DestinationObject, String>(source) {

                                override fun convert(sourceMember: Double) = "${'$'}sourceMember ${'$'}{source.currency}"
                            }
                        """.trimIndent()
                    )
                )
            ),
            Step(
                "Use Converter",
                "Classes declaration",
                listOf(
                    CodeData(
                        Type.KMP,
                        """
                            class SourceObject {
                                //...
                                val cost = 499.99
                            }

                            @KOMMMap(from = [SourceObject::class])
                            data class DestinationObject(
                                //...
                                @MapConvert<SourceObject, DestinationObject, CostConverter>(CostConverter::class)
                                val cost: String
                            ) {
                                //...
                                @MapConvert<SourceObject, DestinationObject, CostConverter>(CostConverter::class, "cost")
                                var otherCost: String = ""
                            }
                        """.trimIndent()
                    )
                )
            ),
            Step(
                "Use Converter",
                "Generated extension function",
                listOf(
                    CodeData(
                        Type.KMP,
                        """
                            fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
                                //...
                                cost = CostConverter(this).convert(cost)
                            ).also { 
                                //...
                                it.otherCost = CostConverter(this).convert(cost)
                            }
                        """.trimIndent()
                    )
                )
            ),
            Step(
                "Context-aware converter",
                "Use a context-aware converter when the conversion also needs values from KOMMMap.context.",
                listOf(
                    CodeData(
                        Type.KMP,
                        """
                            data class AccountMapContext(
                                val banks: Map<Long, Bank>
                            )

                            class BankConverter(
                                source: FullAccount,
                                context: AccountMapContext
                            ) : KOMMContextConverter<FullAccount, Long?, AccountMapContext, Account, Bank?>(source, context) {

                                override fun convert(sourceMember: Long?): Bank? =
                                    sourceMember?.let(context.banks::get)
                            }
                        """.trimIndent()
                    )
                )
            ),
            Step(
                "Context-aware converter",
                "Classes declaration",
                listOf(
                    CodeData(
                        Type.KMP,
                        """
                            @KOMMMap(from = [FullAccount::class], context = AccountMapContext::class)
                            data class Account(
                                //...
                                @MapConvert<FullAccount, Account, BankConverter>(BankConverter::class, "bankId")
                                val bank: Bank?
                            )
                        """.trimIndent()
                    )
                )
            ),
            Step(
                "Context-aware converter",
                "Generated extension function",
                listOf(
                    CodeData(
                        Type.KMP,
                        """
                            fun FullAccount.toAccount(kommContext: AccountMapContext): Account = Account(
                                //...
                                bank = BankConverter(this, kommContext).convert(bankId)
                            )
                        """.trimIndent()
                    )
                )
            )
        )
    )
