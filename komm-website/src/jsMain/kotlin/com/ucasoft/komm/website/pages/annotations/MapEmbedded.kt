package com.ucasoft.komm.website.pages.annotations

import com.ucasoft.komm.website.components.code.CodeData
import com.ucasoft.komm.website.components.code.Type
import com.ucasoft.komm.website.data.DetailItem
import com.ucasoft.komm.website.data.Step
import com.ucasoft.wrappers.lucide.Settings
import react.create

val mapEmbedded = DetailItem(
    Settings.create(),
    "@MapEmbedded",
    """
    Use @MapEmbedded when several destination properties should be mapped from the same nested source property.
    KOMM checks only the first nested level. Direct source properties have priority over embedded properties.
    If two embedded properties can provide the same destination property, generation fails and the mapping should be made explicit.
    """.trimIndent(),
    listOf(
        Step(
            "Classes declaration",
            "",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    data class Account(
                        val id: Long,
                        val name: String
                    )

                    data class AccountWithCurrencies(
                        val account: Account,
                        val currencies: List<AccountCurrency>
                    )

                    @KOMMMap(from = [AccountWithCurrencies::class])
                    @MapEmbedded("account")
                    data class AccountDto(
                        val name: String,
                        val currencies: List<AccountCurrencyDto>
                    ) {
                        var id: Long = 0L
                    }
                """.trimIndent()
                )
            )
        ),
        Step(
            "Generated extension function",
            "",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    fun AccountWithCurrencies.toAccountDto(): AccountDto = AccountDto(
                        name = account.name,
                        currencies = currencies.map { it.toAccountCurrencyDto() }
                    ).also {
                        it.id = account.id
                    }
                """.trimIndent()
                )
            )
        ),
        Step(
            "Nullable embedded source",
            "",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    data class AccountWithCurrencies(
                        val account: Account?,
                        val currencies: List<AccountCurrency>
                    )

                    @KOMMMap(from = [AccountWithCurrencies::class])
                    @MapEmbedded("account")
                    data class AccountDto(
                        @NullSubstitute(MapDefault(StringResolver::class))
                        val name: String,
                        val currencies: List<AccountCurrencyDto>
                    )
                """.trimIndent()
                )
            )
        ),
        Step(
            "Nullable embedded source",
            "Generated extension function",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    fun AccountWithCurrencies.toAccountDto(): AccountDto = AccountDto(
                        name = account?.name ?: StringResolver(null).resolve(),
                        currencies = currencies.map { it.toAccountCurrencyDto() }
                    )
                """.trimIndent()
                )
            )
        )
    )
)
