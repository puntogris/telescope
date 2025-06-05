package com.puntogris.telescope.plugin

import com.android.tools.idea.util.listenUntilNextSync
import com.android.tools.idea.util.runWhenSmartAndSyncedOnEdt
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.ColorUtil
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.UIUtil
import com.puntogris.telescope.ui.pages.HomePage
import com.puntogris.telescope.ui.pages.SettingsPage
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

class ToolWindow : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        createContent(project, toolWindow)
    }
}

private fun createContent(project: Project, toolWindow: ToolWindow) {
    toolWindow.contentManager.removeAllContents(true)
    toolWindow.displayLoading()

    project.runWhenSmartAndSyncedOnEdt(callback = { result ->
        if (result.isSuccessful) {
            displayInToolWindow(toolWindow, project)
        } else {
            toolWindow.displayWaitingForGoodSync()
            project.listenUntilNextSync { createContent(project, toolWindow) }
        }
    })
}

private fun displayInToolWindow(toolWindow: ToolWindow, project: Project) {
    val contentManager = toolWindow.contentManager
    contentManager.removeAllContents(true)

    val home = contentManager.factory.createContent(HomePage(project), "Home", false)
    val settings = contentManager.factory.createContent(SettingsPage(project).createPage(), "Settings", false)

    toolWindow.contentManager.addContent(home)
    toolWindow.contentManager.addContent(settings)
}

private fun ToolWindow.displayWaitingView(message: String, showWarning: Boolean) {
    contentManager.removeAllContents(true)
    val waitingForSyncPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        val waitingLabel = JBLabel().apply {
            text = message
            if (showWarning) {
                icon = AllIcons.General.Warning
            }

            foreground = ColorUtil.toAlpha(UIUtil.getLabelForeground(), 150)
            alignmentX = JComponent.CENTER_ALIGNMENT
            alignmentY = JComponent.CENTER_ALIGNMENT
        }
        add(Box.createVerticalGlue())
        add(waitingLabel)
        add(Box.createVerticalGlue())
    }
    val content = contentManager.factory.createContent(waitingForSyncPanel, null, false)
    contentManager.addContent(content)
}

private fun ToolWindow.displayWaitingForGoodSync() = displayWaitingView("Waiting for successful sync...", true)

private fun ToolWindow.displayLoading() = displayWaitingView("Loading...", false)