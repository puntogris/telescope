package com.puntogris.telescope.domain

import com.intellij.ide.util.PropertiesComponent

private const val AI_MODEL_PATH_KEY = "AI_MODEL_PATH_KEY"
private const val FUZZY_CHECKBOX_STATE_KEY = "FUZZY_CHECKBOX_STATE_KEY"
private const val EMBEDDING_CHECKBOX_STATE_KEY = "EMBEDDING_CHECKBOX_STATE_KEY"
object GlobalStorage {

    fun setModelPath(path: String) {
        PropertiesComponent.getInstance().setValue(AI_MODEL_PATH_KEY, path)
    }

    fun getModelPath(): String {
        return PropertiesComponent.getInstance().getValue(AI_MODEL_PATH_KEY, "")
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
