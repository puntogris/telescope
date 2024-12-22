package com.puntogris.telescope.ui.pages

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.puntogris.telescope.service.ResourcesService
import com.puntogris.telescope.service.SyncService
import com.puntogris.telescope.application.SearchQuery
import com.puntogris.telescope.ui.components.ControlsPanel
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

    private val syncService = SyncService.getInstance(project)
    private val resourcesService = ResourcesService.getInstance(project)
    private val searchQuery = SearchQuery(project)
    private var searchJob: Job? = null

    private val list = ListPanel(
        files = resourcesService.getDrawableResources(),
        onClick = { FileEditorManager.getInstance(project).openFile(it, true) }
    )

    private val controls = ControlsPanel(
        onRefreshClicked = { syncService.sync(onComplete = list::update) }
    )

    private val search = SearchPanel(
        onChange = ::onNewSearch
    )

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        add(controls)
        add(search)
        add(list)
    }

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