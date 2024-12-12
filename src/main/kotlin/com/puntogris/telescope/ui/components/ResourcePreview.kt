package com.puntogris.telescope.ui.components

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import com.kitfox.svg.SVGUniverse
import com.puntogris.telescope.domain.DiskCache
import com.puntogris.telescope.domain.MemoryCache
import com.puntogris.telescope.domain.usecase.VectorToSvg
import com.puntogris.telescope.icons.MyIcons
import com.puntogris.telescope.models.DrawableRes
import com.puntogris.telescope.utils.JPEG
import com.puntogris.telescope.utils.JPG
import com.puntogris.telescope.utils.PNG
import com.puntogris.telescope.utils.WEBP
import com.puntogris.telescope.utils.XML
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.io.StringReader
import javax.imageio.ImageIO
import javax.swing.JPanel

abstract class ResourcePreview {

    abstract fun render(): JPanel

    companion object {
        const val IMAGE_SIZE = 50

        fun from(res: DrawableRes): ResourcePreview {
            val content = res.file.readText()

            return when (res.file.extension) {
                XML -> {
                    if (content.startsWith("<vector")) {
                        VectorPreview(content, res.path, res.module)
                    } else {
                        NotSupportedPreview
                    }
                }

                PNG, JPEG, JPG, WEBP -> RasterPreview(res.file)
                else -> NotSupportedPreview
            }
        }
    }
}

class VectorPreview(
    private val fileContent: String,
    private val path: String,
    private val module: String
) : ResourcePreview() {

    override fun render(): JPanel {
        return try {
            val cached = MemoryCache.svg.getIfPresent(path)

            if (cached != null) {
                getSVGPanel(cached)
            } else {
                val svg = VectorToSvg().invoke(fileContent, module)
                MemoryCache.svg.put(path, svg)
                getSVGPanel(svg)
            }
        } catch (ignored: Throwable) {
            NotSupportedPreview.render()
        }
    }

    private fun getSVGPanel(svgString: String, size: Double = IMAGE_SIZE.toDouble()): JPanel {
        val svgUniverse = SVGUniverse()
        val svgUri = svgUniverse.loadSVG(StringReader(svgString), "svg")
        val svgDiagram = svgUniverse.getDiagram(svgUri)

        return object : JPanel() {
            init {
                preferredSize = Dimension(size.toInt(), size.toInt())
            }

            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                if (g is Graphics2D) {
                    val scale = size / maxOf(svgDiagram.width, svgDiagram.height)
                    val translateX = (size - svgDiagram.width * scale) / 2
                    val translateY = (size - svgDiagram.height * scale) / 2

                    g.translate(translateX, translateY)
                    g.scale(scale, scale)
                    svgDiagram.render(g)

                    g.scale(1 / scale, 1 / scale)
                    g.translate(-translateX, -translateY)
                }
            }
        }
    }
}

class RasterPreview(private val file: VirtualFile) : ResourcePreview() {
    override fun render(): JPanel {
        return try {
            val cached = DiskCache.getIfPresent(file.path)

            val image = if (cached != null) {
                cached
            } else {
                val new = ImageIO.read(file.inputStream).getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_SMOOTH)
                DiskCache.put(new, file.extension.orEmpty(), file.path)
                new
            }
            return object : JPanel() {
                init {
                    preferredSize = Dimension(IMAGE_SIZE, IMAGE_SIZE)
                }

                override fun paintComponent(g: Graphics) {
                    super.paintComponent(g)
                    g.drawImage(image, 0, 0, this)
                }
            }
        } catch (ignored: Throwable) {
            NotSupportedPreview.render()
        }
    }
}

data object NotSupportedPreview : ResourcePreview() {
    override fun render(): JPanel {
        return object : JPanel() {
            init {
                preferredSize = Dimension(IMAGE_SIZE, IMAGE_SIZE)
            }

            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                MyIcons.NotSupportedIcon.paintIcon(this, g, 0, 0)
            }
        }
    }
}
