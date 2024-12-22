package com.puntogris.telescope.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.puntogris.telescope.models.ImageEntity
import com.puntogris.telescope.models.ImageEntity_
import com.puntogris.telescope.models.MyObjectBox
import com.puntogris.telescope.models.SearchResult
import com.puntogris.telescope.utils.configPath
import io.objectbox.Box
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val DB_DIR = "db"

@Service(Service.Level.PROJECT)
class ResourcesDatabase(project: Project) {

    private val databaseDirectory = configPath.resolve(project.name).toFile()

    private val store = MyObjectBox.builder()
        .baseDirectory(databaseDirectory)
        .name(DB_DIR)
        .build()

    private val imagesBox: Box<ImageEntity> by lazy {
        store.boxFor(ImageEntity::class.java)
    }

    suspend fun addBatched(entities: List<ImageEntity>, batchSize: Int = 50) = withContext(Dispatchers.IO) {
        imagesBox.putBatched(entities, batchSize)
    }

    suspend fun getSimilarUri(uri: String): List<SearchResult> = withContext(Dispatchers.IO) {
        val query = imagesBox.query(ImageEntity_.uri.contains(uri, QueryBuilder.StringOrder.CASE_INSENSITIVE)).build()
        val results = query.find()

        results.map {
            SearchResult(
                uri = it.uri
            )
        }
    }

    suspend fun removeAll() = withContext(Dispatchers.IO) {
        imagesBox.removeAll()
    }

    suspend fun getNearestNeighbors(embedding: FloatArray): List<SearchResult> = withContext(Dispatchers.IO) {
        val query = imagesBox.query(ImageEntity_.embedding.nearestNeighbors(embedding, 10)).build()
        val results = query.findWithScores()

        results.map {
            SearchResult(
                uri = it.get().uri,
                score = it.score.toFloat()
            )
        }
    }

    companion object {
        fun getInstance(project: Project): ResourcesDatabase = project.service()
    }
}