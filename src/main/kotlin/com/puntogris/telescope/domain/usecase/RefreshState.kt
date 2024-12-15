package com.puntogris.telescope.domain.usecase

import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.puntogris.telescope.domain.Clip
import com.puntogris.telescope.domain.DrawableCache
import com.puntogris.telescope.domain.ImagesDB
import com.puntogris.telescope.models.DrawableRes
import com.puntogris.telescope.models.ImageEntity
import com.puntogris.telescope.utils.sendNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RefreshState {
    private val getResources = GetResources()
    private val getModelsPath = GetModelsPath()

    operator fun invoke(project: Project, onComplete: ( List<DrawableRes>) -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                ImagesDB.removeAll()
                DrawableCache.createImageCache({}).clear()
                val files = indexFiles(project)
                onComplete(files)
                sendNotification(project, "Telescope sync completed", NotificationType.INFORMATION)
            } catch (e: Exception) {
                thisLogger().error(e)
                sendNotification(project, "Telescope sync completed failed", NotificationType.ERROR)
            }
        }
    }

    private suspend fun indexFiles(project: Project): List<DrawableRes> {
        val drawables = getResources(project).drawables

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
            ImagesDB.addBatched(encodedEntities, 20)
        }
    }

    private suspend fun processInvalidModels(drawables: List<DrawableRes>) {
        val entities = drawables.map { drawable -> ImageEntity(uri = drawable.path) }
        ImagesDB.addBatched(entities, 100)
    }
}