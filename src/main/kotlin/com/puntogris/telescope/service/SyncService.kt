package com.puntogris.telescope.service

import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.puntogris.telescope.application.Clip
import com.puntogris.telescope.storage.DrawableCache
import com.puntogris.telescope.application.GetModelsPath
import com.puntogris.telescope.models.DrawableRes
import com.puntogris.telescope.models.ImageEntity
import com.puntogris.telescope.models.Resources
import com.puntogris.telescope.storage.DiskCache
import com.puntogris.telescope.utils.sendNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Service(Service.Level.PROJECT)
class SyncService(private val project: Project) : Disposable {

    private val disposable = Disposer.newDisposable(this, SyncService::javaClass.name)
    private val resourcesService = ResourcesService.getInstance(project)
    private val databaseService = ResourcesDatabase.getInstance(project)
    private val getModelsPath = GetModelsPath()

    fun sync(onComplete: (List<DrawableRes>) -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                databaseService.removeAll()
                DrawableCache.createImageCache(disposable).clear()
                DiskCache.invalidateAll()
                val files = indexFiles()
                onComplete(files)
                sendNotification(project, "Telescope sync completed", NotificationType.INFORMATION)
            } catch (e: Exception) {
                thisLogger().error(e)
                sendNotification(project, "Telescope sync completed failed", NotificationType.ERROR)
            }
        }
    }

    private suspend fun indexFiles(): List<DrawableRes> {
        val resources = resourcesService.getResources()

        if (getModelsPath().areValid) {
            processValidModels(resources)
        } else {
            processInvalidModels(resources)
        }
        // TODO we should update everything
        return resources.drawables
    }

    private suspend fun processValidModels(resources: Resources) {
        resources.drawables.chunked(100).forEach { chunk ->
            val encodedEntities = chunk.map { drawable ->
                ImageEntity(
                    uri = drawable.path,
                    //TODO not sure about passing colors and dependencies to encode each image
                    embedding = Clip.encodeFileImage(drawable, resources.colors, resources.dependencies)
                        .getOrDefault(floatArrayOf())
                )
            }
            databaseService.addBatched(encodedEntities, 20)
        }
    }

    private suspend fun processInvalidModels(resources: Resources) {
        val entities = resources.drawables.map { drawable -> ImageEntity(uri = drawable.path) }
        databaseService.addBatched(entities, 100)
    }

    override fun dispose() {
        disposable.dispose()
    }

    companion object {
        fun getInstance(project: Project): SyncService = project.service()
    }
}