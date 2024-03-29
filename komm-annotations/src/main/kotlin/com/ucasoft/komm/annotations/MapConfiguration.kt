package com.ucasoft.komm.annotations

annotation class MapConfiguration(
    val tryAutoCast: Boolean = true,
    val allowNotNullAssertion: Boolean = false,
    val mapDefaultAsFallback: Boolean = false,
    val convertFunctionName: String = ""
)