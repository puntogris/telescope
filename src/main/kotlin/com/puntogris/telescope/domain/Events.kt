package com.puntogris.telescope.domain

import com.intellij.util.messages.Topic

interface SettingsListener {
    fun onModelPathUpdated(validPath: Boolean)
}

val SETTINGS_TOPIC = Topic.create("SETTINGS_TOPIC", SettingsListener::class.java)
