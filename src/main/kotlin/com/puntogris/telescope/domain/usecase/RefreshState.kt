package com.puntogris.telescope.domain.usecase

import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.puntogris.telescope.domain.Clip
import com.puntogris.telescope.domain.DiskCache
import com.puntogris.telescope.domain.ImagesDB
import com.puntogris.telescope.domain.MemoryCache
import com.puntogris.telescope.utils.sendNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RefreshState {
    private val getResources = GetResources()

    operator fun invoke(project: Project) {

        CoroutineScope(Dispatchers.Default).launch {
            try {
                MemoryCache.svg.invalidateAll()
                DiskCache.invalidateAll()
                ImagesDB.removeAll()
                indexFiles(project)
                sendNotification(project, "Telescope sync completed", NotificationType.INFORMATION)
            } catch (e: Exception) {
                thisLogger().error(e)
                sendNotification(project, "Telescope sync completed failed", NotificationType.ERROR)
            }
        }
    }

    private suspend fun indexFiles(project: Project) {
        getResources(project).drawables.forEach {
            // TODO if we dont have the models paths we need to avoid this and just save the paths
            // we could get the path and check if are valid
            Clip.encodeFileImage(it).onSuccess { emb ->
                ImagesDB.add(it.path, emb)
            }
        }
    }
}