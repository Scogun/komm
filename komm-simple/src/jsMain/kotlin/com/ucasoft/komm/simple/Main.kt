package com.ucasoft.komm.simple

import kotlinx.browser.document

fun main () {
    document.body!!.innerText = SourceObject().toDestinationObject().toString()
}