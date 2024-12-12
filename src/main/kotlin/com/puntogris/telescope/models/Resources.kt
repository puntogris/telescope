package com.puntogris.telescope.models

data class Resources(
    val drawables: List<DrawableRes>,
    val colors: Map<String, Map<String, String>>,
    val dependencies: Map<String, List<String>>
)