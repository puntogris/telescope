package com.puntogris.telescope.storage

import com.puntogris.telescope.utils.PNG
import com.puntogris.telescope.utils.configPath
import com.puntogris.telescope.utils.toBufferedImage
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import javax.imageio.ImageIO
import javax.swing.SwingWorker
import kotlin.io.path.absolutePathString

// TODO would could make this a service
object DiskCache {

    private const val CACHE_DIR = "cache"

    private var cacheDir: String? = null

    fun init(projectName: String) {
        if (cacheDir == null) {
            cacheDir = getCacheDir(projectName)
        }
    }

    fun put(image: Image, path: String) {
        if (cacheDir == null) {
            return
        }
        val worker = object : SwingWorker<Unit, Unit>() {
            override fun doInBackground() {
                try {
                    val dir = File(cacheDir, path.hashCode().toString())
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }
                    ImageIO.write(image.toBufferedImage(), PNG, dir)
                } catch (ignored: Throwable) {
                }
            }
        }
        worker.execute()
    }

    fun getIfPresent(path: String): BufferedImage? {
        if (cacheDir == null) {
            return null
        }
        val dir = File(cacheDir, path.hashCode().toString())

        if (dir.exists()) {
            return ImageIO.read(dir)
        }
        return null
    }

    fun invalidateAll() {
        if (cacheDir == null) {
            return
        }
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