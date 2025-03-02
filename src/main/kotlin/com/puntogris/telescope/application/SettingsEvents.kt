package com.puntogris.telescope.application

import com.intellij.util.messages.Topic

interface SettingsEvents {
    fun embeddingsModelDownloaded()

    companion object {
        val TOPIC = Topic.create("Settings Events", SettingsEvents::class.java)
    }
}