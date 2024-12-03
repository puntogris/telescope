package com.puntogris.telescope.domain

import com.intellij.ide.util.PropertiesComponent

private const val TEXT_MODEL_PATH_KEY = "TEXT_MODEL_PATH_KEY"
private const val VISION_MODEL_PATH_KEY = "VISION_MODEL_PATH_KEY"
private const val FUZZY_CHECKBOX_STATE_KEY = "FUZZY_CHECKBOX_STATE_KEY"
private const val EMBEDDING_CHECKBOX_STATE_KEY = "EMBEDDING_CHECKBOX_STATE_KEY"

object GlobalStorage {

    fun setTextModelPath(path: String) {
        PropertiesComponent.getInstance().setValue(TEXT_MODEL_PATH_KEY, path)
    }

    fun getTextModelPath(): String {
        return PropertiesComponent.getInstance().getValue(TEXT_MODEL_PATH_KEY, "")
    }

    fun setVisionModelPath(path: String) {
        PropertiesComponent.getInstance().setValue(VISION_MODEL_PATH_KEY, path)
    }

    fun getVisionModelPath(): String {
        return PropertiesComponent.getInstance().getValue(VISION_MODEL_PATH_KEY, "")
    }

    fun setFuzzyState(state: Boolean) {
        PropertiesComponent.getInstance().setValue(FUZZY_CHECKBOX_STATE_KEY, state)
    }

    fun getFuzzyState(): Boolean {
        return PropertiesComponent.getInstance().getBoolean(FUZZY_CHECKBOX_STATE_KEY, true)
    }

    fun setEmbeddingsState(state: Boolean) {
        PropertiesComponent.getInstance().setValue(EMBEDDING_CHECKBOX_STATE_KEY, state)
    }

    fun getEmbeddingsState(): Boolean {
        return PropertiesComponent.getInstance().getBoolean(EMBEDDING_CHECKBOX_STATE_KEY, false)
    }
}
