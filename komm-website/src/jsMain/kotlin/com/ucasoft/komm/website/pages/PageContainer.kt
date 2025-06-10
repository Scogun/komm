package com.ucasoft.komm.website.pages

import com.ucasoft.wrappers.lucide.House
import mui.material.Box
import mui.material.Breadcrumbs
import mui.material.Link
import mui.material.Typography
import mui.system.sx
import react.FC
import react.PropsWithChildren
import react.ReactNode
import web.cssom.AlignItems
import web.cssom.Color
import web.cssom.Display
import web.cssom.px
import react.router.dom.Link as RouterLink

data class BreadCrumb(val icon: ReactNode, val label: String, val path: String)

external interface PageContainerProps : PropsWithChildren {
    var homePath: String
    var breadcrumbs: List<BreadCrumb>
}

val PageContainer = FC<PageContainerProps> {
    Box {
        sx {
            padding = 10.px
        }
        if (it.breadcrumbs.isNotEmpty()) {
            Breadcrumbs {
                ariaLabel = "breadcrumb"
                sx {
                    marginBottom = 3.px
                }
                Link {
                    component = RouterLink
                    asDynamic().to = "/"
                    sx {
                        display = Display.flex
                        alignItems = AlignItems.center
                        color = Color("text.secondary")
                        hover {
                            color = Color("primary.main")
                        }
                    }
                    House {}
                    +"Home"
                }
                it.breadcrumbs.mapIndexed { index, breadcrumb ->
                    if (index == it.breadcrumbs.size - 1) {
                        Typography {
                            sx {
                                color = Color("text.primary")
                                display = Display.flex
                                alignItems = AlignItems.center
                            }
                            +breadcrumb.icon
                            +breadcrumb.label
                        }
                    } else {
                        Link {
                            component = RouterLink
                            asDynamic().to = breadcrumb.path
                            sx {
                                display = Display.flex
                                alignItems = AlignItems.center
                                color = Color("text.secondary")
                                hover {
                                    color = Color("primary.main")
                                }
                            }
                            +breadcrumb.icon
                            +breadcrumb.label
                        }
                    }
                }
            }
        }
        +it.children
    }
}