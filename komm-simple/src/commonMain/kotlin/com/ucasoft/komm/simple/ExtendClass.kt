package com.ucasoft.komm.simple

import com.ucasoft.komm.abstractions.KOMMConverter
import com.ucasoft.komm.annotations.KOMMMap
import com.ucasoft.komm.annotations.MapConvert
import com.ucasoft.komm.annotations.MapFrom

@KOMMMap(from = SourceObject::class)
data class DestinationObject(
    val id: Int,
    val intToString: String,
    val stringToInt: Int,
    @MapFrom("userName")
    val name: String,
    @MapConvert<CostConverter>(converter = CostConverter::class)
    val cost: String
)

class CostConverter(source: SourceObject) : KOMMConverter<SourceObject, Double, String>(source) {

    override fun convert(sourceMember: Double) = "$sourceMember ${source.currency}"
}