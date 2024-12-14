package com.puntogris.telescope.ui.pages

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.puntogris.telescope.domain.usecase.GetResources
import com.puntogris.telescope.domain.usecase.RefreshState
import com.puntogris.telescope.domain.usecase.SearchQuery
import com.puntogris.telescope.ui.components.CheckboxPanel
import com.puntogris.telescope.ui.components.ListPanel
import com.puntogris.telescope.ui.components.SearchPanel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import javax.swing.BoxLayout
import javax.swing.JPanel

class HomePage(project: Project) : JPanel() {

    private val list = ListPanel(
        files = GetResources().invoke(project).drawables,
        onClick = { FileEditorManager.getInstance(project).openFile(it, true) }
    )

    private val checkbox = CheckboxPanel(
        onRefreshClicked = {
            RefreshState().invoke(project)
            list.update(GetResources().invoke(project).drawables)
        }
    )

    private val search = SearchPanel(
        onChange = ::onNewSearch
    )

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        add(checkbox)
        add(search)
        add(list)
    }

    private var searchJob: Job? = null
    private val searchQuery = SearchQuery()

    private fun onNewSearch(query: String) {
        searchJob?.cancel()

        if (query.isBlank()) {
            list.reset()
        } else {
            searchJob = CoroutineScope(Dispatchers.Swing).launch {
                val result = searchQuery(query)
                list.filter(result)
            }
        }
    }
}