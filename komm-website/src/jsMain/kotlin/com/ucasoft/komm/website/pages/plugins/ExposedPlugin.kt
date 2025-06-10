package com.ucasoft.komm.website.pages.plugins

import com.ucasoft.komm.website.components.code.CodeData
import com.ucasoft.komm.website.components.code.Type
import com.ucasoft.komm.website.data.DetailItem
import com.ucasoft.komm.website.data.Step
import com.ucasoft.wrappers.lucide.Database
import react.create

val exposedPlugin = DetailItem(
    Database.create(),
    "Exposed Plugin",
    "Provides mapping from Exposed Table Objects (ResultRow) to your data classes for easy database interaction.",
    listOf(
        Step(
            "Installation",
            "Add with Gradle",
            listOf(
                CodeData(
                    Type.JVM, """
                plugins {
                    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
                }

                val kommVersion = "0.25.0"

                depensencies {
                    implementation("com.ucasoft.komm:komm-annotations:${'$'}kommVersion")
                    ksp("com.ucasoft.komm:komm-processor:${'$'}kommVersion")
                    ksp("com.ucasoft.komm:komm-plugins-exposed:${'$'}kommVersion")
                }
            """.trimIndent()
                )
            )
        ),
        Step(
            "Usage",
            "Classes declaration",
            listOf(
                CodeData(
                    Type.JVM, """
                object SourceObject: Table() {
                    val id = integer("id").autoIncrement()
                    val name = varchar("name", 255)
                    val age = integer("age")
                
                    override val primaryKey = PrimaryKey(id)
                }
                
                @KOMMMap(from = [SourceObject::class])
                data class DestinationObject(
                    val id: Int,
                    val name: String,
                    val age: Int
                )
            """.trimIndent()
                )
            )
        ),
        Step(
            "Usage",
            "Generated extension function",
            listOf(
                CodeData(
                    Type.JVM, """
                public fun ResultRow.toDestinationObject(): DestinationObject = DestinationObject(
                    id = this[SourceObject.id],
                    name = this[SourceObject.name],
                    age = this[SourceObject.age]
                )
            """.trimIndent()
                )
            )
        )
    )
)