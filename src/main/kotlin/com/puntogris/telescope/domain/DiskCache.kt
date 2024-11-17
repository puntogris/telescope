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

    private var projectName: String? = null
    private var cacheDir: String? = null

    fun init(name: String) {
        projectName = name
        cacheDir = getCacheDir()
    }

    fun put(image: Image, format: String, path: String) {
        val worker = object : SwingWorker<Unit, Unit>() {
            override fun doInBackground() {
                try {
                    val dir = File(getCacheDir(), path.hashCode().toString())
                    ImageIO.write(toBufferedImage(image), format.uppercase(), dir)
                } catch (ignored: Throwable) {
                }

            }
        }
        worker.execute()
    }

    fun getIfPresent(path: String): BufferedImage? {
        val dir = File(getCacheDir(), path.hashCode().toString())
        if (dir.exists()) {
            return ImageIO.read(dir)
        }
        return null
    }

    private fun getCacheDir(): String? {
        if (cacheDir != null || projectName == null) {
            return cacheDir
        }

        val pluginCacheDir = PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_PACKAGE))
        val pluginPath = pluginCacheDir?.pluginPath

        if (pluginPath != null) {
            val cacheDir = File(pluginPath.absolutePathString(), "cache/$projectName")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            this.cacheDir = cacheDir.absolutePath
        }
        return cacheDir
    }
}