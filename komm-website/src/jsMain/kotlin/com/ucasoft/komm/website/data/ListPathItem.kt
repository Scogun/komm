package com.ucasoft.komm.website.data

import react.ComponentType
import react.Props
import react.ReactNode

class ListPathItem(
    icon: ReactNode,
    title: String,
    path: String,
    component: ComponentType<Props>,
    val listItems: List<IconItem>,
    description: String = ""
) : PathItem(icon, title, path, component, description)