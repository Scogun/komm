package com.ucasoft.komm.simple

fun main() {
    val source = SourceObject()
    val destination = source.toDestinationObject()
    println(destination)
    println(destination.intToString)
}