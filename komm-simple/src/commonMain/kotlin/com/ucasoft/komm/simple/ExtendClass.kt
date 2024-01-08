package com.ucasoft.komm.simple

import com.ucasoft.komm.abstractions.KOMMConverter
import com.ucasoft.komm.abstractions.KOMMResolver
import com.ucasoft.komm.annotations.KOMMMap
import com.ucasoft.komm.annotations.MapConvert
import com.ucasoft.komm.annotations.MapDefault
import com.ucasoft.komm.annotations.MapFrom
import java.time.Instant
import java.util.*

@KOMMMap(from = SourceObject::class)
data class DestinationObject(
    val id: Int,
    val stringToInt: Int,
    @MapFrom("userName")
    val name: String,
    @MapConvert<CostConverter>(converter = CostConverter::class)
    val cost: String,
    @MapDefault<DateResolver>(DateResolver::class)
    val activeDate: Date
) {
    var intToString: String = ""

    @MapDefault<DateResolver>(DateResolver::class)
    var otherDate: Date = Date.from(Instant.now())

    @MapConvert<CostConverter>(name = "cost", converter = CostConverter::class)
    var otherCost: String = ""
}

class CostConverter(source: SourceObject) : KOMMConverter<SourceObject, Double, String>(source) {

    override fun convert(sourceMember: Double) = "$sourceMember ${source.currency}"
}

class DateResolver(destination: DestinationObject?) : KOMMResolver<DestinationObject, Date>(destination) {
    override fun resolve(): Date = Date.from(Instant.now())
}

@KOMMMap(from = SourceObject::class)
data class SecondDestinationObject(
    val id: Int,
    val stringToInt: Int
)

@KOMMMap(from = Currency::class)
data class ExCurrency(
    val symbol: String,
    val numericCode: String
)