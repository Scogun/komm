package com.ucasoft.komm.website.pages.annotations

import com.ucasoft.komm.website.data.DetailItem
import com.ucasoft.wrappers.lucide.Settings
import react.create

val annotationData = listOf(
    kommMap,
    DetailItem(Settings.create(), "@MapName", "Provides possibility to map properties with different names"),
    DetailItem(Settings.create(), "@MapConvert", "Provides possibility to add additional logic for properties mapping"),
    DetailItem(Settings.create(), "@MapDefault", "Provides possibility to add default values for orphans properties"),
    DetailItem(Settings.create(), "@NullSubstitute", "Extends mapping from nullable type properties")
)