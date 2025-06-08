package com.ucasoft.komm.website.data

import react.ReactNode

class DetailItem(icon: ReactNode, title: String, description: String = "", val steps: List<Step> = emptyList()): IconItem(icon, title, description)