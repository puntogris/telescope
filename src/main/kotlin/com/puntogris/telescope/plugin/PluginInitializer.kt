package com.puntogris.telescope.plugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.puntogris.telescope.domain.DiskCache
import com.puntogris.telescope.domain.ImagesDB

class PluginInitializer : ProjectActivity {
    override suspend fun execute(project: Project) {

        //TODO are we sure this always run before the window?
        ImagesDB.init(project.name)
        DiskCache.init(project.name)
    }
}

