package com.ucasoft.komm.website.data

import react.ReactNode

open class IconItem(val icon: ReactNode, val title: String, val description: String = "") {
    val id: String
        get() = title.trimStart('@').replace(" ", "").replaceFirstChar { it.lowercase() }
}