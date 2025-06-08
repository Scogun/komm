package com.ucasoft.komm.website.pages.plugins

import com.ucasoft.komm.website.data.ListPathItem
import com.ucasoft.komm.website.pages.ListPage
import react.FC
import react.router.useLoaderData


val Plugins = FC {

    val data = useLoaderData().unsafeCast<ListPathItem>()

    ListPage {
        icon = data.icon
        title = data.title
        items = data.listItems
    }
}