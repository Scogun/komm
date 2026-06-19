package com.ucasoft.komm.website.data

import react.ComponentType
import react.Props
import react.ReactNode

open class PathItem(
    icon: ReactNode,
    title: String,
    val path: String,
    val component: ComponentType<Props>,
    description: String = ""
) : IconItem(icon, title, description)