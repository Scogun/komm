package com.ucasoft.komm.website.pages.annotations

import com.ucasoft.komm.website.components.code.CodeData
import com.ucasoft.komm.website.components.code.Type
import com.ucasoft.komm.website.data.DetailItem
import com.ucasoft.komm.website.data.Step
import com.ucasoft.wrappers.lucide.Settings
import react.create

val mapTargetDefault = DetailItem(
    Settings.create(),
    "@MapTargetDefault",
    "Defines target property defaults at class level for mappings into classes that cannot be annotated.",
    listOf(
        Step(
            "Classes declaration",
            "Declare source mapping and class-level default metadata for the external target property.",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    data class AccountCardMapContext(val accountId: Long)

                    class AccountIdResolver(
                        destination: DbAccountCard?,
                        context: AccountCardMapContext
                    ) : KOMMContextResolver<AccountCardMapContext, DbAccountCard, Long>(destination, context) {

                        override fun resolve(): Long = context.accountId
                    }

                    @KOMMMap(to = [DbAccountCard::class], context = AccountCardMapContext::class)
                    @MapTargetDefault(
                        name = "accountId",
                        default = MapDefault(AccountIdResolver::class),
                        `for` = [DbAccountCard::class]
                    )
                    data class AccountCard(
                        val id: Long,
                        val type: String,
                        val number: String
                    )
                """.trimIndent()
                )
            )
        ),
        Step(
            "Generated extension function",
            "The generated mapper resolves the target default while creating the external model.",
            listOf(
                CodeData(
                    Type.KMP,
                    """
                    fun AccountCard.toDbAccountCard(kommContext: AccountCardMapContext): DbAccountCard = DbAccountCard(
                        id = id,
                        accountId = AccountIdResolver(null, kommContext).resolve(),
                        type = type,
                        number = number
                    )
                """.trimIndent()
                )
            )
        )
    )
)
