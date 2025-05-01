package com.ucasoft.komm.simple

import com.ucasoft.komm.abstractions.KOMMConverter
import com.ucasoft.komm.abstractions.KOMMResolver
import com.ucasoft.komm.annotations.*

@KOMMMap(from = [SourceObject::class], to = [], config = MapConfiguration(allowNotNullAssertion = true, tryAutoCast = true, mapDefaultAsFallback = false, convertFunctionName = ""))
data class DestinationObject(
    val id: Int,
    val stringToInt: Int,
    @MapName("userName", [])
    val name: String,
    @MapConvert<SourceObject, DestinationObject, DestinationCostConverter>(converter = DestinationCostConverter::class, "")
    val cost: String,
    @NullSubstitute(MapDefault(StringResolver::class), "nullable", [])
    val notNullable: String,
    @MapName("iAmInt", [])
    val iAmNullable: Int?
) {
    var intToString: String = ""

    @MapConvert<SourceObject, DestinationObject, DestinationCostConverter>(name = "cost", converter = DestinationCostConverter::class)
    var otherCost: String = ""

    @MapName("nullable", [])
    var otherNullable: Int? = 1
}

class DestinationCostConverter(source: SourceObject) : CostConverter<DestinationObject>(source)

open class CostConverter<T>(source: SourceObject) : KOMMConverter<SourceObject, Double, T, String>(source) {

    override fun convert(sourceMember: Double) = "$sourceMember ${source.currency}"
}

class StringResolver(destination: DestinationObject?): KOMMResolver<DestinationObject, String>(destination) {

    override fun resolve() = "123"
}

@KOMMMap(from = [SourceObject::class], to = [], config = MapConfiguration(allowNotNullAssertion = false, tryAutoCast = true, mapDefaultAsFallback = false, convertFunctionName = ""))
data class SecondDestinationObject(
    val id: Int,
    val stringToInt: Int
)