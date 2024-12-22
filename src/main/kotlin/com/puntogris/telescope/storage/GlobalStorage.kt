package com.puntogris.telescope.storage

import com.intellij.ide.util.PropertiesComponent

private const val DEFAULT_TEXT_MODEL_PATH_KEY = "TELESCOPE_DEFAULT_TEXT_MODEL_PATH"
private const val DEFAULT_VISION_MODEL_PATH_KEY = "TELESCOPE_DEFAULT_VISION_MODEL_PATH"
private const val CUSTOM_TEXT_MODEL_PATH_KEY = "TELESCOPE_CUSTOM_TEXT_MODEL_PATH"
private const val CUSTOM_VISION_MODEL_PATH_KEY = "TELESCOPE_CUSTOM_VISION_MODEL_PATH"
private const val PARTIAL_MATCH_CHECKBOX_STATE_KEY = "TELESCOPE_PARTIAL_MATCH_CHECKBOX_STATE"
private const val EMBEDDING_CHECKBOX_STATE_KEY = "TELESCOPE_EMBEDDING_CHECKBOX_STATE"
private const val USE_DEFAULT_MODELS_KEY = "TELESCOPE_USE_DEFAULT_MODELS"

private const val EMPTY_STRING = ""

object GlobalStorage {

    fun setDefaultTextModelPath(value: String) {
        PropertiesComponent.getInstance().setValue(DEFAULT_TEXT_MODEL_PATH_KEY, value)
    }

    fun getDefaultTextModelPath(): String {
        return PropertiesComponent.getInstance().getValue(DEFAULT_TEXT_MODEL_PATH_KEY, EMPTY_STRING)
    }

    fun setDefaultVisionModelPath(value: String) {
        PropertiesComponent.getInstance().setValue(DEFAULT_VISION_MODEL_PATH_KEY, value)
    }

    fun getDefaultVisionModelPath(): String {
        return PropertiesComponent.getInstance().getValue(DEFAULT_VISION_MODEL_PATH_KEY, EMPTY_STRING)
    }

    fun setCustomTextModelPath(value: String) {
        PropertiesComponent.getInstance().setValue(CUSTOM_TEXT_MODEL_PATH_KEY, value)
    }

    fun getCustomTextModelPath(): String {
        return PropertiesComponent.getInstance().getValue(CUSTOM_TEXT_MODEL_PATH_KEY, EMPTY_STRING)
    }

    fun setCustomVisionModelPath(value: String) {
        PropertiesComponent.getInstance().setValue(CUSTOM_VISION_MODEL_PATH_KEY, value)
    }

    fun getCustomVisionModelPath(): String {
        return PropertiesComponent.getInstance().getValue(CUSTOM_VISION_MODEL_PATH_KEY, EMPTY_STRING)
    }

    fun sePartialMatchState(value: Boolean) {
        PropertiesComponent.getInstance().setSecureBoolean(PARTIAL_MATCH_CHECKBOX_STATE_KEY, value)
    }

    fun getPartialMatchState(): Boolean {
        return PropertiesComponent.getInstance().getSecureBoolean(PARTIAL_MATCH_CHECKBOX_STATE_KEY, true)
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

    // TODO There seems to be a bug where booleans are not saved correctly
    // workaround for this, should investigate more, seems to fail when we use a default value
    private fun PropertiesComponent.getSecureBoolean(name: String, default: Boolean): Boolean {
        return getValue(name).orEmpty().toBooleanStrictOrNull() ?: default
    }

    private fun PropertiesComponent.setSecureBoolean(name: String, value: Boolean) {
        setValue(name, value.toString())
    }
}