package com.ucasoft.komm.simple

import com.ucasoft.komm.abstractions.KOMMConverter
import com.ucasoft.komm.abstractions.KOMMResolver
import com.ucasoft.komm.annotations.*

@KOMMMap(from = SourceObject::class, config = MapConfiguration(allowNotNullAssertion = true, tryAutoCast = true, mapDefaultAsFallback = false, convertFunctionName = ""))
data class DestinationObject(
    val id: Int,
    val stringToInt: Int,
    @MapFrom("userName", [])
    val name: String,
    @MapConvert<SourceObject, CostConverter>(converter = CostConverter::class, "")
    val cost: String,
    @NullSubstitute(MapDefault(StringResolver::class), "nullable", [])
    val notNullable: String,
    @MapFrom("iAmInt", [])
    val iAmNullable: Int?
) {
    var intToString: String = ""

    @MapConvert<SourceObject, CostConverter>(name = "cost", converter = CostConverter::class)
    var otherCost: String = ""

    @MapFrom("nullable", [])
    var otherNullable: Int? = 1
}

class CostConverter(source: SourceObject) : KOMMConverter<SourceObject, Double, String>(source) {

    override fun convert(sourceMember: Double) = "$sourceMember ${source.currency}"
}

class StringResolver(destination: DestinationObject?): KOMMResolver<DestinationObject, String>(destination) {

    override fun resolve() = "123"
}

@KOMMMap(from = SourceObject::class, config = MapConfiguration(allowNotNullAssertion = false, tryAutoCast = true, mapDefaultAsFallback = false, convertFunctionName = ""))
data class SecondDestinationObject(
    val id: Int,
    val stringToInt: Int
)