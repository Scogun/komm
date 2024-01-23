package com.ucasoft.komm.simple

import com.ucasoft.komm.simple.other.toThirdDestinationObject
import java.util.*

fun main() {
    val source = SourceObject()
    val destination = source.toJvmDestinationObject()
    println(destination)
    println(source.toSecondDestinationObject())
    println(source.toThirdDestinationObject())

    val c = Currency.getInstance(Locale.US)
    println(c.toExCurrency())
}