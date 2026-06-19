package com.ucasoft.komm.sample.other

import com.ucasoft.komm.annotations.KOMMMap
import com.ucasoft.komm.annotations.MapConfiguration
import com.ucasoft.komm.sample.SourceObject

@KOMMMap(from = [SourceObject::class], to = [], config = MapConfiguration(allowNotNullAssertion = false, tryAutoCast = true, mapDefaultAsFallback = false, convertFunctionName = ""))
data class ThirdDestinationObject(
    val id: Int,
    val stringToInt: Int
)