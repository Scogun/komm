package com.ucasoft.komm.simple

import com.ucasoft.komm.simple.other.toThirdDestinationObject

fun main() {
    val source = SourceObject()
    val destination = source.toDestinationObject()
    println(destination)
    println(destination.intToString)
    println(source.toSecondDestinationObject())
    println(source.toThirdDestinationObject())
}