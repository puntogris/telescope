package com.puntogris.telescope.domain

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.puntogris.telescope.utils.toBufferedImage
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.SwingWorker
import kotlin.io.path.absolutePathString

object DiskCache {

    private const val PLUGIN_PACKAGE = "com.puntogris.telescope"

    private var cacheDir: String? = null

    fun init(projectName: String) {
        cacheDir = getCacheDir(projectName)
    }

    fun put(image: Image, format: String, path: String) {
        val worker = object : SwingWorker<Unit, Unit>() {
            override fun doInBackground() {
                try {
                    val dir = File(cacheDir, path.hashCode().toString())
                    ImageIO.write(image.toBufferedImage(), format.uppercase(), dir)
                } catch (ignored: Throwable) {
                }
            }
        }
        worker.execute()
    }

    fun getIfPresent(path: String): BufferedImage? {
        val dir = File(cacheDir, path.hashCode().toString())
        if (dir.exists()) {
            return ImageIO.read(dir)
        }
        return null
    }

    private fun getCacheDir(projectName: String): String? {
        if (cacheDir != null) {
            return cacheDir
        }

        val pluginCacheDir = PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_PACKAGE))
        val pluginPath = pluginCacheDir?.pluginPath

        if (pluginPath != null) {
            val newCacheDir = File(pluginPath.absolutePathString(), "cache/$projectName")
            if (!newCacheDir.exists()) {
                newCacheDir.mkdirs()
            }
            cacheDir = newCacheDir.absolutePath
        }
        return cacheDir
    }
}