package com.puntogris.telescope.ui.pages

import com.intellij.openapi.application.EDT
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
import javax.swing.BoxLayout
import javax.swing.JPanel

class HomePage(project: Project) : JPanel() {

    private val syncService = SyncService.getInstance(project)
    private val resourcesService = ResourcesService.getInstance(project)
    private val searchQuery = SearchQuery(project)
    private var searchJob: Job? = null

    private val list = ListPanel(
        resources = resourcesService.getResources(),
        onClick = { FileEditorManager.getInstance(project).openFile(it, true) }
    )

    private val controls = ControlsPanel(
        project = project,
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

        syncService.init()
    }

    private fun onNewSearch(query: String) {
        searchJob?.cancel()

        if (query.isBlank()) {
            list.reset()
        } else {
            searchJob = CoroutineScope(Dispatchers.EDT).launch {
                val result = searchQuery(query)
                list.filter(result)
            }
        }
    }
}