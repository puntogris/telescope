package com.puntogris.telescope.domain

import com.intellij.ide.util.PropertiesComponent

private const val TEXT_MODEL_PATH_KEY = "TELESCOPE_TEXT_MODEL_PATH"
private const val VISION_MODEL_PATH_KEY = "TELESCOPE_VISION_MODEL_PATH"
private const val FUZZY_CHECKBOX_STATE_KEY = "TELESCOPE_FUZZY_CHECKBOX_STATE"
private const val EMBEDDING_CHECKBOX_STATE_KEY = "TELESCOPE_EMBEDDING_CHECKBOX_STATE"
private const val USE_DEFAULT_MODELS_KEY = "TELESCOPE_USE_DEFAULT_MODELS"

object GlobalStorage {

    fun setTextModelPath(value: String) {
        PropertiesComponent.getInstance().setValue(TEXT_MODEL_PATH_KEY, value)
    }

    fun getTextModelPath(): String {
        return PropertiesComponent.getInstance().getValue(TEXT_MODEL_PATH_KEY, "")
    }

    fun setVisionModelPath(value: String) {
        PropertiesComponent.getInstance().setValue(VISION_MODEL_PATH_KEY, value)
    }

    fun getVisionModelPath(): String {
        return PropertiesComponent.getInstance().getValue(VISION_MODEL_PATH_KEY, "")
    }

    fun setFuzzyState(value: Boolean) {
        PropertiesComponent.getInstance().setSecureBoolean(FUZZY_CHECKBOX_STATE_KEY, value)
    }

    fun getFuzzyState(): Boolean {
        return PropertiesComponent.getInstance().getSecureBoolean(FUZZY_CHECKBOX_STATE_KEY, true)
    }

    fun setEmbeddingsState(value: Boolean) {
        PropertiesComponent.getInstance().setSecureBoolean(EMBEDDING_CHECKBOX_STATE_KEY, value)
    }

    fun getEmbeddingsState(): Boolean {
        return PropertiesComponent.getInstance().getSecureBoolean(EMBEDDING_CHECKBOX_STATE_KEY, false)
    }

    fun setUseDefaultModels(value: Boolean) {
        PropertiesComponent.getInstance().setSecureBoolean(USE_DEFAULT_MODELS_KEY, value)
    }

    fun getUseDefaultModels(): Boolean {
        return PropertiesComponent.getInstance().getSecureBoolean(USE_DEFAULT_MODELS_KEY, true)
    }

    // There seems to be a bug where booleans are not saved correctly
    // workaround for this, should investigate more, seems to fail when we use a default value
    private fun PropertiesComponent.getSecureBoolean(name: String, default: Boolean): Boolean {
        return getValue(name).orEmpty().toBooleanStrictOrNull() ?: default
    }

    private fun PropertiesComponent.setSecureBoolean(name: String, value: Boolean) {
        setValue(name, value.toString())
    }
}
