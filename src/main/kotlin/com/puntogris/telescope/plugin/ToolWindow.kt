package com.puntogris.telescope.plugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.puntogris.telescope.ui.pages.HomePage
import com.puntogris.telescope.ui.pages.SettingsPage

class ToolWindow : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentManager = toolWindow.contentManager

        val home = contentManager.factory.createContent(HomePage(project), "Home", false)
        val settings = contentManager.factory.createContent(SettingsPage(project).createContent(), "Settings", false)

        toolWindow.contentManager.addContent(home)
        toolWindow.contentManager.addContent(settings)
    }
}
