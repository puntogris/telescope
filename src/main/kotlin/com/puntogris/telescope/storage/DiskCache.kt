package com.puntogris.telescope.storage

import com.puntogris.telescope.utils.configPath
import com.puntogris.telescope.utils.toBufferedImage
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import javax.imageio.ImageIO
import javax.swing.SwingWorker
import kotlin.io.path.absolutePathString

object DiskCache {

    private const val CACHE_DIR = "cache"

    private var cacheDir: String? = null

    fun init(projectName: String) {
        if (cacheDir == null) {
            cacheDir = getCacheDir(projectName)
        }
    }

    fun put(image: Image, format: String, path: String) {
        val worker = object : SwingWorker<Unit, Unit>() {
            override fun doInBackground() {
                try {
                    val dir = File(cacheDir, path.hashCode().toString() + ".$format")
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }
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

    fun invalidateAll() {
        val worker = object : SwingWorker<Unit, Unit>() {
            override fun doInBackground() {
                try {
                    File(requireNotNull(cacheDir)).deleteRecursively()
                } catch (ignored: Throwable) {
                }
            }
        }
        worker.execute()
    }

    private fun getCacheDir(projectName: String): String? {
        val configPath = configPath.resolve(projectName).resolve(CACHE_DIR)
        Files.createDirectories(configPath)

        cacheDir = configPath.absolutePathString()
        return cacheDir
    }
}