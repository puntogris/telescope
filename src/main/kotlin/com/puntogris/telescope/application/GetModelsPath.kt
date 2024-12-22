package com.puntogris.telescope.application

import com.puntogris.telescope.models.ModelsPath
import com.puntogris.telescope.storage.GlobalStorage

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