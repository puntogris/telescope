package com.puntogris.telescope.models

data class Resources(
    val drawables: List<DrawableRes>,
    val colors: Colors,
    val dependencies: Dependencies
)

typealias Colors = Map<String, Map<String, String>>
typealias Dependencies = Map<String, List<String>>