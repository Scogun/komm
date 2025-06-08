package com.ucasoft.komm.website.data

import react.ComponentType
import react.ReactNode

open class PathItem(icon: ReactNode, title: String, val path: String, val component: ComponentType<*>, description: String = "") : IconItem(icon, title, description)