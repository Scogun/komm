package com.ucasoft.komm.website.pages

import com.ucasoft.komm.website.DetailItem
import com.ucasoft.komm.website.IconItem
import com.ucasoft.wrappers.lucide.Puzzle
import com.ucasoft.wrappers.lucide.Tag
import react.FC
import react.create
import react.router.useLoaderData

val DetailPage = FC {

    val item = useLoaderData().unsafeCast<DetailItem>()
    val parentIcon = if (item.title.endsWith("Plugin")) Puzzle.create() else Tag.create()
    val parentTitle = if (item.title.endsWith("Plugin")) "Plugins" else "Annotations"

    val breadCrumbs = listOf(
        BreadCrumb(parentIcon, parentTitle, "/${parentTitle.lowercase()}"),
        BreadCrumb(item.icon, item.title, item.title)
    )

    PageContainer {
        homePath = "Home"
        breadcrumbs = breadCrumbs
        +item.description
    }
}