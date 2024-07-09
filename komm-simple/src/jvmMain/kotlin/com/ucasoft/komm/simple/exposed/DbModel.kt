package com.ucasoft.komm.simple.exposed

import org.jetbrains.exposed.sql.Table

object DbModel: Table() {
    val id = integer("id").autoIncrement()
    val user = varchar("user", 50)
    val age = integer("ago")


    override val primaryKey = PrimaryKey(id)
}