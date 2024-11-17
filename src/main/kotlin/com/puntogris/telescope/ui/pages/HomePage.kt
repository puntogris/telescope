package com.puntogris.telescope.ui.pages

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.puntogris.telescope.domain.Files
import com.puntogris.telescope.ui.components.CheckboxPanel
import com.puntogris.telescope.ui.components.ListPanel
import com.puntogris.telescope.ui.components.SearchPanel
import javax.swing.BoxLayout
import javax.swing.JPanel

class HomePage(project: Project) : JPanel() {

    private val list = ListPanel(
        files = Files.getResDirectories(project),
        onClick = {
            FileEditorManager.getInstance(project).openFile(it, true)
        }
    )

    private val checkbox = CheckboxPanel(
        project = project,
        onRefreshClicked = {
            Files.refresh()
        }
    )

    private val search = SearchPanel(
        onChange = list::filter
    )

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        add(checkbox)
        add(search)
        add(list)
    }
}