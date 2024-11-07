package com.puntogris.telescope

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findDirectory
import com.intellij.openapi.vfs.readText
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.puntogris.telescope.domain.Convert
import com.puntogris.telescope.domain.VectorDrawableConverter
import com.puntogris.telescope.ui.CheckboxPanel
import com.puntogris.telescope.ui.ListPanel
import com.puntogris.telescope.ui.SearchPanel
import javax.swing.*

class ToolWindow : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, window: ToolWindow) {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        val list = ListPanel(
            files = getResDirectories(project),
            onClick = {
                val svg = VectorDrawableConverter().transform(it.readText())
                thisLogger().warn(svg)
                val fileEditorManager = FileEditorManager.getInstance(project)
                fileEditorManager.openFile(it, true)
            }
        )

        val checkbox = CheckboxPanel()
        val search = SearchPanel(
            onChange = {
                list.filter(it)
            }
        )

        panel.add(checkbox)
        panel.add(search)
        panel.add(list)

        val contentManager = window.contentManager
        val content = contentManager.factory.createContent(panel, null, false)
        window.contentManager.addContent(content)
    }

    private fun getResDirectories(project: Project): List<VirtualFile> {
        return project.baseDir.children
            .mapNotNull { it.findDirectory("src/main/res/drawable") }
            .flatMap { it.children.toList() }
    }
}

