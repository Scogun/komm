package com.ucasoft.komm.sample.exposed.database

import org.jetbrains.exposed.v1.core.Table

object DbModel: Table() {
    val id = integer("id").autoIncrement()
    val user = varchar("user", 50)
    val age = integer("ago")


    override val primaryKey = PrimaryKey(id)
}
