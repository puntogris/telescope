package com.puntogris.telescope.domain.usecase

import com.puntogris.telescope.domain.Clip
import com.puntogris.telescope.domain.GlobalStorage
import com.puntogris.telescope.domain.ImagesDB
import com.puntogris.telescope.models.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchUseCase {
    suspend operator fun invoke(query: String): List<SearchResult> = withContext(Dispatchers.Default) {
        buildList {
            if (GlobalStorage.getFuzzyState()) {
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