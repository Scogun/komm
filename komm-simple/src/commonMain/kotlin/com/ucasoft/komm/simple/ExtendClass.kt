package com.ucasoft.komm.simple

import com.ucasoft.komm.annotations.KOMMMap

@KOMMMap(from = [SourceObject::class])
data class DestinationObject(
    val id: Int,
    val intToString: String,
    val stringToInt: Int
)