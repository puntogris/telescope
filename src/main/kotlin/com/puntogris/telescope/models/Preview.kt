package com.puntogris.telescope.models

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import com.puntogris.telescope.domain.DiskCache
import com.puntogris.telescope.domain.MemoryCache
import com.puntogris.telescope.domain.VectorDrawableConverter
import com.puntogris.telescope.icons.MyIcons
import com.puntogris.telescope.ui.components.SVGPanel
import com.puntogris.telescope.utils.*
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Image
import javax.imageio.ImageIO
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.SwingWorker

private const val IMAGE_SIZE = 50

sealed class Preview {

    abstract fun render(): JPanel

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

    class XmlVector(private val fileContent: String, val path: String) : Preview() {
        override fun render(): JPanel {
            return try {
                val cached = MemoryCache.svg.getIfPresent(path)

                if (cached == null) {
                    val svg = VectorDrawableConverter().transform(fileContent).replaceColors()
                    MemoryCache.svg.put(path, svg)
                    SVGPanel(svg)
                } else {
                    SVGPanel(cached)
                }
            } catch (ignored: Throwable) {
                defaultPreview
            }
        }
    }

    class RasterImage(private val file: VirtualFile) : Preview() {
        override fun render(): JPanel {
            return try {
                val cachedImage = DiskCache.getIfPresent(file.path)

                val img = if (cachedImage != null) {
                    cachedImage
                } else {
                    val image = ImageIO.read(file.inputStream).getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_SMOOTH)
                    DiskCache.put(image, file.extension.orEmpty(), file.path)
                    image
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

    protected fun String.replaceColors(): String {
        val fillRegex = """fill="[^"]*"""".toRegex()
        val strokeRegex = """stroke="[^"]*"""".toRegex()

        return replace(fillRegex, """fill="#000000"""").replace(strokeRegex, """stroke="#000000"""")
    }

    companion object {

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

class RasterTest(private val file: VirtualFile) : JPanel() {

    private var image: Image? = null

    private val worker = object : SwingWorker<Image, Void>() {
        override fun doInBackground(): Image? {
            return try {
                val image = ImageIO.read(file.inputStream)
                image.getScaledInstance(50, 50, Image.SCALE_SMOOTH)

            } catch (e: Throwable) {
                null
            }
        }

        override fun done() {
            image = get()
            thisLogger().warn("logeto__${image == null}")
            SwingUtilities.invokeLater {
                repaint()
                revalidate()
            }
        }
    }

    init {
        preferredSize = Dimension(50, 50)

        SwingUtilities.invokeLater {
            val a = ImageIO.read(file.inputStream).getScaledInstance(50, 50, Image.SCALE_SMOOTH)
            image = a
            repaint()
            invalidate()
            revalidate()
        }

    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        thisLogger().warn("logeto__painting")

        if (image != null) {
            MyIcons.NotSupportedIcon.paintIcon(this, g, 0, 0)

        } else {
            thisLogger().warn("logeto__no_image_to_draw")
        }
    }
}
