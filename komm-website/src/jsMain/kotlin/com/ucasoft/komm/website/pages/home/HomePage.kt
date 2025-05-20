package com.ucasoft.komm.website.pages.home

import com.ucasoft.komm.website.Features
import com.ucasoft.komm.website.Hero
import com.ucasoft.komm.website.Targets
import mui.material.Box
import react.FC

val HomePage = FC {
    Box {
        Hero {}
        Features {}
        Targets {}
    }
}