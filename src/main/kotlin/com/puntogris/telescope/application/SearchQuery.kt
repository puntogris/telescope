package com.puntogris.telescope.application

import com.intellij.openapi.project.Project
import com.puntogris.telescope.models.SearchResult
import com.puntogris.telescope.service.ResourcesDatabase
import com.puntogris.telescope.service.ResourcesService
import com.puntogris.telescope.storage.GlobalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.xdrop.fuzzywuzzy.FuzzySearch

class SearchQuery(project: Project) {

    private val databaseService = ResourcesDatabase.getInstance(project)
    private val resourcesService = ResourcesService.getInstance(project)

    suspend operator fun invoke(query: String): List<SearchResult> = withContext(Dispatchers.Default) {
        buildList {
            if (GlobalStorage.getFuzzyMatchState()) {
                val matches = FuzzySearch.extractSorted(
                    query,
                    resourcesService.currentResources.drawables,
                    { it.file.nameWithoutExtension },
                    50
                ).map { SearchResult(uri = it.referent.path) }
                addAll(matches)
            }
            if (GlobalStorage.getEmbeddingsState()) {
                Clip.encodeText(query).onSuccess { emb ->
                    val matches = databaseService.getNearestNeighbors(emb)
                    val newMatches = matches.filter { m -> none { it.uri == m.uri } }
                    addAll(newMatches)
                }
            }
        }
    }
}