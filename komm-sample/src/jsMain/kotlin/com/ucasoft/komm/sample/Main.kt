package com.ucasoft.komm.sample

import kotlinx.browser.document

fun main () {
    document.body!!.innerText = SourceObject().toDestinationObject().toString()
}
