package com.ucasoft.komm.website.data

import react.ComponentType
import react.ReactNode

class ListPathItem(
    icon: ReactNode,
    title: String,
    path: String,
    component: ComponentType<*>,
    val listItems: List<IconItem>,
    description: String = ""
) : PathItem(icon, title, path, component, description)