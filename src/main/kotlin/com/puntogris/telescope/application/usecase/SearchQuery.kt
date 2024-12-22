package com.puntogris.telescope.application.usecase

import com.puntogris.telescope.application.Clip
import com.puntogris.telescope.storage.GlobalStorage
import com.puntogris.telescope.storage.ImagesDB
import com.puntogris.telescope.models.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchQuery {

    suspend operator fun invoke(query: String): List<SearchResult> = withContext(Dispatchers.Default) {
        buildList {
            if (GlobalStorage.getPartialMatchState()) {
                val matches = ImagesDB.getSimilarUri(query)
                addAll(matches)
            }
            if (GlobalStorage.getEmbeddingsState()) {
                Clip.encodeText(query).onSuccess { emb ->
                    val matches = ImagesDB.getNearestNeighbors(emb)
                    val newMatches = matches.filter { m -> none { it.uri == m.uri } }
                    addAll(newMatches)
                }
            }
        }
    }
}