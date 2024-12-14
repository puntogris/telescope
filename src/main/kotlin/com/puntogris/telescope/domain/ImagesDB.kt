package com.puntogris.telescope.domain

import com.puntogris.telescope.models.ImageEntity
import com.puntogris.telescope.models.ImageEntity_
import com.puntogris.telescope.models.MyObjectBox
import com.puntogris.telescope.models.SearchResult
import com.puntogris.telescope.utils.configPath
import io.objectbox.Box
import io.objectbox.BoxStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImagesDB {

    private const val DB_DIR = "db"

    private lateinit var store: BoxStore

    fun init(projectName: String) {
        if (this::store.isInitialized) {
            return
        }
        val configPath = configPath.resolve(projectName)

        store = MyObjectBox.builder()
            .baseDirectory(configPath.toFile())
            .name(DB_DIR)
            .build()
    }

    private val imagesBox: Box<ImageEntity> by lazy {
        store.boxFor(ImageEntity::class.java)
    }

    suspend fun add(uri: String, embedding: FloatArray) = withContext(Dispatchers.IO) {
        imagesBox.put(ImageEntity(uri = uri, embedding = embedding))
    }

    suspend fun addBatched(entities: List<ImageEntity>, batchSize: Int = 50) = withContext(Dispatchers.IO) {
        imagesBox.putBatched(entities, batchSize)
    }

    suspend fun getSimilarUri(uri: String): List<SearchResult> = withContext(Dispatchers.IO) {
        val query = imagesBox.query(ImageEntity_.uri.contains(uri)).build()

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
        val query = imagesBox
            .query(ImageEntity_.embedding.nearestNeighbors(embedding, 10))
            .build()

        val results = query.findWithScores()

        results.map {
            SearchResult(
                uri = it.get().uri,
                score = it.score.toFloat()
            )
        }
    }
}