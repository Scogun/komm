package com.ucasoft.komm.sample.exposed

import com.ucasoft.komm.annotations.KOMMMap
import com.ucasoft.komm.sample.exposed.database.DbModel

@KOMMMap(from = [DbModel::class])
data class DtoModel(val id: Int, val user: String, val age: Int)