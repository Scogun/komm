package com.ucasoft.komm.simple.other

import com.ucasoft.komm.annotations.KOMMMap
import com.ucasoft.komm.simple.SourceObject

@KOMMMap(from = SourceObject::class)
data class ThirdDestinationObject(
    val id: Int,
    val stringToInt: Int
)