package com.puntogris.telescope.ui.pages

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findDirectory
import com.puntogris.telescope.ui.components.CheckboxPanel
import com.puntogris.telescope.ui.components.ListPanel
import com.puntogris.telescope.ui.components.SearchPanel
import javax.swing.BoxLayout
import javax.swing.JPanel

class HomePage(project: Project): JPanel() {

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        val list = ListPanel(
            files = getResDirectories(project),
            onClick = {
                val fileEditorManager = FileEditorManager.getInstance(project)
                fileEditorManager.openFile(it, true)
            }
        )

        val checkbox = CheckboxPanel(
            onRefreshClicked = {

            },
            project
        )
        val search = SearchPanel(
            onChange = {
                list.filter(it)
            }
        )

        add(checkbox)
        add(search)
        add(list)
    }

    private fun getResDirectories(project: Project): List<VirtualFile> {
        return project.baseDir.children
            .filter { it.isDirectory }
            .mapNotNull { it.findDirectory("src/main/res/drawable") }
            .flatMap { it.children.toList() }
    }
}