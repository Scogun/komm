package com.ucasoft.komm.website

import react.create
import react.dom.client.createRoot
import web.dom.document

fun main() {
    createRoot(document.createElement("div").also {
        it.style.height = "100%"
        document.body.appendChild(it)
    }).render(App.create())
}