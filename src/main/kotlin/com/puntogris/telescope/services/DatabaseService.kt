package com.puntogris.telescope.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.puntogris.telescope.models.ImageEntity
import com.puntogris.telescope.models.ImageEntity_
import com.puntogris.telescope.models.MyObjectBox
import com.puntogris.telescope.models.SearchResult
import com.puntogris.telescope.utils.configPath
import io.objectbox.Box
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val DB_DIR = "db"

@Service(Service.Level.PROJECT)
class DatabaseService(project: Project) : Disposable {

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

    suspend fun removeAll() = withContext(Dispatchers.IO) {
        imagesBox.removeAll()
    }

    suspend fun remove(entities: List<ImageEntity>) = withContext(Dispatchers.IO) {
        imagesBox.remove(entities)
    }

    suspend fun getNearestNeighbors(embedding: FloatArray): List<SearchResult> = withContext(Dispatchers.IO) {
        val query = imagesBox.query(ImageEntity_.embedding.nearestNeighbors(embedding, 30)).build()
        val results = query.findWithScores()

        results.map {
            SearchResult(
                uri = it.get().uri,
                score = it.score.toFloat()
            )
        }
    }

    suspend fun getAll(): List<ImageEntity> = withContext(Dispatchers.IO) {
        imagesBox.all
    }

    companion object {
        fun getInstance(project: Project): DatabaseService = project.service()
    }

    override fun dispose() {
        store.close()
    }
}