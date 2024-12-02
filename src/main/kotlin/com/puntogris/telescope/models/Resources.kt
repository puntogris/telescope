package com.puntogris.telescope.models

data class Resources(
    val drawablesRes: List<DrawableRes>,
    val colorsRes: Map<String, Map<String, String>>
)