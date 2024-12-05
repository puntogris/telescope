package com.puntogris.telescope.plugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.puntogris.telescope.ui.pages.HomePage
import com.puntogris.telescope.ui.pages.SettingsPage

class ToolWindow : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, window: ToolWindow) {
        val contentManager = window.contentManager

        val home = contentManager.factory.createContent(HomePage(project), "Home", false)
        val settings = contentManager.factory.createContent(SettingsPage(project).createContent(), "Settings", false)

        window.contentManager.addContent(home)
        window.contentManager.addContent(settings)
    }
}
