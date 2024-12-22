package com.puntogris.telescope.service

import com.android.tools.idea.concurrency.coroutineScope
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.puntogris.telescope.application.Clip
import com.puntogris.telescope.storage.DrawableCache
import com.puntogris.telescope.application.GetModelsPath
import com.puntogris.telescope.models.DrawableRes
import com.puntogris.telescope.models.ImageEntity
import com.puntogris.telescope.utils.sendNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Service(Service.Level.PROJECT)
class SyncService(private val project: Project) {

    private val resourcesService = ResourcesService.getInstance(project)
    private val databaseService = ResourcesDatabase.getInstance(project)
    private val getModelsPath = GetModelsPath()

    fun sync(onComplete: (List<DrawableRes>) -> Unit) {
        project.coroutineScope.launch(Dispatchers.Default) {
            try {
                databaseService.removeAll()
                DrawableCache.createImageCache({}).clear()
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
        val drawables = resourcesService.getDrawableResources()

        if (getModelsPath().areValid) {
            processValidModels(drawables)
        } else {
            processInvalidModels(drawables)
        }
        return drawables
    }

    private suspend fun processValidModels(drawables: List<DrawableRes>) {
        drawables.chunked(100).forEach { chunk ->
            val encodedEntities = chunk.map { drawable ->
                ImageEntity(
                    uri = drawable.path,
                    embedding = Clip.encodeFileImage(drawable).getOrDefault(floatArrayOf())
                )
            }
            databaseService.addBatched(encodedEntities, 20)
        }
    }

    private suspend fun processInvalidModels(drawables: List<DrawableRes>) {
        val entities = drawables.map { drawable -> ImageEntity(uri = drawable.path) }
        databaseService.addBatched(entities, 100)
    }

    companion object {
        fun getInstance(project: Project): SyncService = project.service()
    }
}