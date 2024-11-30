package com.puntogris.telescope.domain

import com.intellij.openapi.application.PluginPathManager
import java.nio.file.Paths
import java.nio.file.Files
import kotlin.io.path.absolutePathString

object Database {

    fun init(projectName: String) {
        val pluginDataPath = Paths.get(PluginPathManager.getPluginHomePath("telescope"))
        val dbName = projectName.replace(" ", "").replace(".", "").plus(".db")
        val dbPath = pluginDataPath.resolve(dbName).absolutePathString()

        Files.createDirectories(pluginDataPath)
    }
}
