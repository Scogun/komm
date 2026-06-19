package com.ucasoft.komm.sample

import com.ucasoft.komm.sample.other.toThirdDestinationObject
import java.util.*

fun main() {
    val source = SourceObject()
    val destination = source.toJvmDestinationObject()
    println(destination)
    println(source.toSecondDestinationObject())
    println(source.toThirdDestinationObject())
    println(EmbeddedSourceObject(EmbeddedDetails(1L, "Main"), "Embedded sample").toEmbeddedDestinationObject())

    val c = Currency.getInstance(Locale.US)
    println(c.toExCurrency())
}
