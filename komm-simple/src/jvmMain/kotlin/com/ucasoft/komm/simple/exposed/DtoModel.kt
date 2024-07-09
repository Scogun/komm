package com.ucasoft.komm.simple.exposed

import com.ucasoft.komm.annotations.KOMMMap

@KOMMMap(from = DbModel::class)
data class DtoModel(val id: Int, val user: String, val age: Int)