package com.ucasoft.komm.sample

import kotlinx.browser.document

fun main () {
    document.body!!.innerText = listOf(
        SourceObject().toDestinationObject(),
        EmbeddedSourceObject(EmbeddedDetails(1L, "Main"), "Embedded sample").toEmbeddedDestinationObject()
    ).joinToString(separator = "\n")
}
