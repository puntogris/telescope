package com.puntogris.telescope.services

import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.puntogris.telescope.storage.DrawableCache
import com.puntogris.telescope.application.GetModelsPath
import com.puntogris.telescope.models.Colors
import com.puntogris.telescope.models.Dependencies
import com.puntogris.telescope.models.DrawableRes
import com.puntogris.telescope.models.ImageEntity
import com.puntogris.telescope.models.Resources
import com.puntogris.telescope.storage.DiskCache
import com.puntogris.telescope.utils.sendNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Service(Service.Level.PROJECT)
class SyncService(private val project: Project) : Disposable {

    private val disposable = Disposer.newDisposable(this, SyncService::javaClass.name)
    private val resourcesService = ResourcesService.getInstance(project)
    private val databaseService = DatabaseService.getInstance(project)
    private val clipService = ClipService.getInstance(project)
    private val getModelsPath = GetModelsPath()

    private var syncJob: Job? = null

    fun init() {
        syncJob = CoroutineScope(Dispatchers.Default).launch {
            DrawableCache.createImageCache(disposable).clear()
            indexFiles()
        }
    }

    fun sync(onComplete: (List<DrawableRes>) -> Unit) {
        syncJob?.cancel()
        syncJob = CoroutineScope(Dispatchers.Default).launch {
            withBackgroundProgress(project, "Syncing Telescope") {
                try {
                    databaseService.removeAll()
                    DrawableCache.createImageCache(disposable).clear()
                    DiskCache.invalidateAll()
                    val files = indexFiles()
                    onComplete(files.drawables)
                    sendNotification(project, "Telescope sync completed", NotificationType.INFORMATION)
                } catch (e: Exception) {
                    thisLogger().error(e)
                    sendNotification(project, "Telescope sync completed failed", NotificationType.ERROR)
                }
            }
        }
    }

    private suspend fun indexFiles(): Resources {
        val resources = resourcesService.getResources()

        if (getModelsPath().areValid) {
            processEmbeddingsModels(resources)
        } else {
            processDefaultModels(resources)
        }
        return resources
    }

    private suspend fun processEmbeddingsModels(resources: Resources) {
        val drawables = resources.drawables
        val inDb = databaseService.getAll()
        val toAdd = mutableListOf<DrawableRes>()
        val toDelete = mutableListOf<ImageEntity>()

        for (drawable in drawables) {
            val match = inDb.find { it.uri == drawable.path }
            if (match == null || match.timestamp < drawable.file.timeStamp || match.embedding.isEmpty()) {
                toAdd.add(drawable)
            }
        }
        for (entity in inDb) {
            val match = drawables.find { it.path == entity.uri }
            if (match == null) {
                toDelete.add(entity)
            }
        }

        databaseService.remove(toDelete)

        toAdd.chunked(100).forEach { chunk ->
            val encodedEntities = chunk.map { drawable ->
                encodeDrawable(drawable, resources.colors, resources.dependencies)
            }
            databaseService.addBatched(encodedEntities, 20)
        }
    }

    private fun encodeDrawable(
        drawable: DrawableRes,
        colors: Colors,
        dependencies: Dependencies
    ): ImageEntity {
        val embedding = clipService.encodeFileImage(drawable, colors, dependencies).getOrDefault(floatArrayOf())

        return ImageEntity(
            name = drawable.name,
            uri = drawable.path,
            timestamp = drawable.file.timeStamp,
            embedding = embedding
        )
    }

    private suspend fun processDefaultModels(resources: Resources) {
        databaseService.removeAll()
        val entities = resources.drawables.map { drawable ->
            ImageEntity(
                name = drawable.name,
                uri = drawable.path,
                timestamp = drawable.file.timeStamp
            )
        }
        databaseService.addBatched(entities, 100)
    }

    override fun dispose() {
        syncJob?.cancel()
        disposable.dispose()
    }

    companion object {
        fun getInstance(project: Project): SyncService = project.service()
    }
}