package com.ucasoft.komm.website

import react.ComponentType
import react.ReactNode

open class IconItem(val icon: ReactNode, val title: String, val description: String = "")

open class PathItem(icon: ReactNode, title: String, val path: String, val component: ComponentType<*>, description: String = "") : IconItem(icon, title, description)

class ListPathItem(icon: ReactNode, title: String, path: String, component: ComponentType<*>, val listItems: List<IconItem>, description: String = ""): PathItem(icon, title, path, component, description)
