package com.puntogris.telescope.ui.components

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import com.puntogris.telescope.domain.DiskCache
import com.puntogris.telescope.domain.MemoryCache
import com.puntogris.telescope.domain.VectorDrawableConverter
import com.puntogris.telescope.icons.MyIcons
import com.puntogris.telescope.utils.*
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Image
import javax.imageio.ImageIO
import javax.swing.JPanel

abstract class Preview {

    protected val defaultPreview by lazy {
        object : JPanel() {
            init {
                preferredSize = Dimension(IMAGE_SIZE, IMAGE_SIZE)
            }

            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                MyIcons.NotSupportedIcon.paintIcon(this, g, 0, 0)
            }
        }
    }

    abstract fun render(): JPanel

    companion object {

        const val IMAGE_SIZE = 50

        fun from(file: VirtualFile): Preview {
            val content = file.readText()

            return when (file.extension) {
                XML -> {
                    if (content.startsWith("<vector")) {
                        XmlVector(content, file.path)
                    } else {
                        NotSupported
                    }
                }

                PNG, JPEG, JPG, WEBP -> RasterImage(file)
                else -> NotSupported
            }
        }
    }
}

class XmlVector(private val fileContent: String, val path: String) : Preview() {
    override fun render(): JPanel {
        return try {
            val cached = MemoryCache.svg.getIfPresent(path)

            if (cached != null) {
                SVGPanel(cached)
            } else {
                val svg = VectorDrawableConverter().transform(fileContent).replaceUnknownColors()
                MemoryCache.svg.put(path, svg)
                SVGPanel(svg)
            }
        } catch (ignored: Throwable) {
            defaultPreview
        }
    }
}

class RasterImage(private val file: VirtualFile) : Preview() {
    override fun render(): JPanel {
        return try {
            val cached = DiskCache.getIfPresent(file.path)

            val img = if (cached != null) {
                cached
            } else {
                val img = ImageIO.read(file.inputStream).getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_SMOOTH)
                DiskCache.put(img, file.extension.orEmpty(), file.path)
                img
            }
            return object : JPanel() {
                init {
                    preferredSize = Dimension(IMAGE_SIZE, IMAGE_SIZE)
                }

                override fun paintComponent(g: Graphics) {
                    super.paintComponent(g)
                    g.drawImage(img, 0, 0, this)
                }
            }
        } catch (ignored: Throwable) {
            defaultPreview
        }
    }
}

data object NotSupported : Preview() {
    override fun render(): JPanel {
        return defaultPreview
    }
}