package com.puntogris.telescope.models

data class ModelsPath(
    val textModel: String,
    val visionModel: String
) {
    val areValid: Boolean = textModel.isNotEmpty() && visionModel.isNotEmpty()
}