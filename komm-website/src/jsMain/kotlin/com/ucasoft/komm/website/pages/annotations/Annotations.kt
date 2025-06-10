package com.ucasoft.komm.website.pages.annotations

import com.ucasoft.komm.website.data.ListPathItem
import com.ucasoft.komm.website.pages.ListPage
import react.FC
import react.router.useLoaderData

val Annotations = FC {

    val data = useLoaderData().unsafeCast<ListPathItem>()

    ListPage {
        icon = data.icon
        title = data.title
        items = data.listItems
    }
}