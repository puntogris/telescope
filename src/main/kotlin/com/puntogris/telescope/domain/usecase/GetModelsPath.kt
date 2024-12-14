package com.puntogris.telescope.domain.usecase

import com.puntogris.telescope.domain.GlobalStorage

class GetModelsPath {

    operator fun invoke(): ModelsPath {
        return if (GlobalStorage.getUseDefaultModels()) {
            ModelsPath(
                textModel = GlobalStorage.getDefaultTextModelPath(),
                visionModel = GlobalStorage.getDefaultVisionModelPath()
            )
        } else {
            ModelsPath(
                textModel = GlobalStorage.getCustomTextModelPath(),
                visionModel = GlobalStorage.getCustomVisionModelPath()
            )
        }
    }
}

data class ModelsPath(
    val textModel: String,
    val visionModel: String
) {
    val areValid: Boolean = textModel.isNotEmpty() && visionModel.isNotEmpty()
}