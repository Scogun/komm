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
            ),
            Step(
                "Context-aware resolver",
                "Use a context-aware resolver when the default value depends on KOMMMap.context.",
                listOf(
                    CodeData(
                        Type.KMP,
                        """
                            data class TransactionMapContext(
                                val accounts: Map<Long, Account>,
                                val accountCurrencies: Map<Long, AccountCurrency>,
                                val categories: Map<Long, Category>
                            )

                            class FallbackAccountResolver(
                                destination: Transaction?,
                                context: TransactionMapContext
                            ) : KOMMContextResolver<TransactionMapContext, Transaction, Account?>(destination, context) {

                                override fun resolve(): Account? {
                                    return context.accounts.values.firstOrNull()
                                }
                            }
                        """.trimIndent()
                    )
                )
            ),
            Step(
                "Context-aware resolver",
                "Classes declaration",
                listOf(
                    CodeData(
                        Type.KMP,
                        """
                            @KOMMMap(from = [DbTransaction::class], context = TransactionMapContext::class)
                            data class Transaction(
                                //...
                                @MapDefault<FallbackAccountResolver>(FallbackAccountResolver::class)
                                val expenseAccount: Account?
                            )
                        """.trimIndent()
                    )
                )
            ),
            Step(
                "Context-aware resolver",
                "Generated extension function",
                listOf(
                    CodeData(
                        Type.KMP,
                        """
                            fun DbTransaction.toTransaction(kommContext: TransactionMapContext): Transaction = Transaction(
                                //... 
                                expenseAccount = FallbackAccountResolver(null, kommContext).resolve()
                            )
                        """.trimIndent()
                    )
                )
            )
        )
    )
