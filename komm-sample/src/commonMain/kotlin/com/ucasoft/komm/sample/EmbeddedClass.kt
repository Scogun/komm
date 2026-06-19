package com.ucasoft.komm.sample

import com.ucasoft.komm.annotations.KOMMMap
import com.ucasoft.komm.annotations.MapEmbedded
import com.ucasoft.komm.annotations.MapConfiguration

data class EmbeddedDetails(
    val id: Long,
    val name: String
)

data class EmbeddedSourceObject(
    val details: EmbeddedDetails,
    val description: String
)

@KOMMMap(from = [EmbeddedSourceObject::class], to = [], config = MapConfiguration(allowNotNullAssertion = false, tryAutoCast = true, mapDefaultAsFallback = false, convertFunctionName = ""))
@MapEmbedded("details")
data class EmbeddedDestinationObject(
    val id: Long,
    val name: String,
    val description: String
)
