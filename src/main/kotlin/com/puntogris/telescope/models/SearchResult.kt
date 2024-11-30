package com.puntogris.telescope.models

data class SearchResult(
    val uri: String,
    val score: Float = 1F
)