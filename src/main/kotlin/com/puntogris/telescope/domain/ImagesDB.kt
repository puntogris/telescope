package com.puntogris.telescope.domain

import com.intellij.openapi.application.PluginPathManager
import com.puntogris.telescope.models.ImageEntity
import com.puntogris.telescope.models.ImageEntity_
import com.puntogris.telescope.models.MyObjectBox
import com.puntogris.telescope.models.SearchResult
import io.objectbox.Box
import io.objectbox.BoxStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Paths

object ImagesDB {

    private lateinit var store: BoxStore

    fun init(projectName: String) {
        if (this::store.isInitialized) {
            return
        }
        val pluginPath = Paths.get(PluginPathManager.getPluginHomePath("telescope"))
        val dbPath = pluginPath.resolve(projectName)
        store = MyObjectBox.builder()
            .baseDirectory(File("build/idea-sandbox/plugins/telescope/db4"))
            .name("test")
            .build()
    }

    private val imagesBox: Box<ImageEntity> by lazy {
        store.boxFor(ImageEntity::class.java)
    }

    suspend fun add(uri: String, embedding: FloatArray) = withContext(Dispatchers.IO) {
        imagesBox.put(ImageEntity(uri = uri, embedding = embedding))
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