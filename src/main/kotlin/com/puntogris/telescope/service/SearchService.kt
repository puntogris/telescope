package com.puntogris.telescope.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.puntogris.telescope.application.Clip
import com.puntogris.telescope.models.SearchResult
import com.puntogris.telescope.storage.GlobalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.xdrop.fuzzywuzzy.FuzzySearch

@Service(Service.Level.PROJECT)
class SearchService(project: Project) {

    private val databaseService = DatabaseService.getInstance(project)
    private val resourcesService = ResourcesService.getInstance(project)

    suspend fun search(query: String): List<SearchResult> = withContext(Dispatchers.Default) {
        buildList {
            if (GlobalStorage.getIsFuzzySearchEnabled()) {
                val matches = FuzzySearch.extractSorted(
                    query,
                    resourcesService.currentResources.drawables,
                    { it.file.nameWithoutExtension },
                    50
                ).map { SearchResult(uri = it.referent.path) }
                addAll(matches)
            }
            if (GlobalStorage.getIsEmbeddingsSearchEnabled()) {
                Clip.encodeText(query).onSuccess { emb ->
                    val matches = databaseService.getNearestNeighbors(emb).filter { m -> none { it.uri == m.uri } }
                    addAll(matches)
                }
            }
        }
    }
}