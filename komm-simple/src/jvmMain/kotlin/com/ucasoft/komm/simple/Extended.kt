package com.ucasoft.komm.simple

import com.ucasoft.komm.abstractions.KOMMResolver
import com.ucasoft.komm.annotations.*
import java.time.Instant
import java.util.*

@KOMMMap(from = [SourceObject::class])
data class JvmDestinationObject(
    val id: Int,
    val stringToInt: Int,
    @MapName("userName")
    val name: String,
    @MapConvert<SourceObject, JvmDestinationObject, JvmDestinationCostConverter>(converter = JvmDestinationCostConverter::class)
    val cost: String,
    @MapDefault<DateResolver>(DateResolver::class)
    val activeDate: Date,
    @NullSubstitute(MapDefault(StringResolver::class), "nullable")
    val notNullable: String,
    @MapName("iAmInt")
    val iAmNullable: Int?
) {
    @MapDefault<DateResolver>(DateResolver::class)
    var otherDate: Date = Date.from(Instant.now())
}

class JvmDestinationCostConverter(source: SourceObject): CostConverter<JvmDestinationObject>(source)

class DateResolver(destination: JvmDestinationObject?) : KOMMResolver<JvmDestinationObject, Date>(destination) {
    override fun resolve(): Date = Date.from(Instant.now())
}

@KOMMMap(from = [Currency::class])
data class ExCurrency(
    val symbol: String,
    val numericCode: String
)