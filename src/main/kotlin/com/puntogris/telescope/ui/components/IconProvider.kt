package com.puntogris.telescope.ui.components

import com.android.tools.idea.gradle.variant.conflict.displayName
import com.android.tools.idea.ui.resourcemanager.plugin.DesignAssetRendererManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.readText
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.ui.JBImageIcon
import com.kitfox.svg.SVGUniverse
import com.puntogris.telescope.application.VectorToSvg
import com.puntogris.telescope.storage.DrawableCache
import com.puntogris.telescope.icons.MyIcons
import com.puntogris.telescope.models.Colors
import com.puntogris.telescope.models.Dependencies
import com.puntogris.telescope.models.DrawableRes
import com.puntogris.telescope.utils.JPEG
import com.puntogris.telescope.utils.PNG
import com.puntogris.telescope.utils.WEBP
import com.puntogris.telescope.utils.toBufferedImage
import com.puntogris.telescope.utils.toImage
import java.awt.Dimension
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.StringReader
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import javax.imageio.ImageIO
import javax.swing.Icon

private const val DEFAULT_SIZE = 50

class IconProvider : Disposable {

    private val disposable = Disposer.newDisposable(this, IconProvider::javaClass.name)
    private val imageIcon = JBImageIcon(MyIcons.NotSupportedIcon.toImage())
    private val fetchImageExecutor = service<FetchImageExecutor>()
    private val drawableCache = DrawableCache.createImageCache(disposable)
    private val drawableRenderer = DrawableRenderer()
    private val placeholder = BufferedImage(50, 50, 1)

    fun getIcon(
        drawable: DrawableRes,
        refreshCallback: () -> Unit,
        shouldBeRendered: () -> Boolean,
        colors: Colors,
        dependencies: Dependencies
    ): Icon {
        val image = drawableCache.computeAndGet(drawable.file, placeholder, false, refreshCallback) {
            if (shouldBeRendered()) {
                CompletableFuture.supplyAsync(
                    { if (shouldBeRendered()) drawableRenderer.render(drawable, colors, dependencies) else null },
                    fetchImageExecutor
                )
            } else {
                CompletableFuture.completedFuture(null)
            }
        }
        imageIcon.image = image
        return imageIcon
    }

    @Service
    private class FetchImageExecutor : ExecutorService by AppExecutorUtil.createBoundedApplicationPoolExecutor(
        FetchImageExecutor::class.java.simpleName,
        1
    )

    override fun dispose() {
        disposable.dispose()
    }
}

class DrawableRenderer {
    private val rendererManager = DesignAssetRendererManager.getInstance()
    private val vectorToSvg = VectorToSvg()
    private val rasterExtensions = listOf(PNG, WEBP, JPEG)

    fun render(
        drawable: DrawableRes,
        colors: Colors,
        dependencies: Dependencies,
        size: Int = DEFAULT_SIZE
    ): BufferedImage? {
        try {
            val content = drawable.file.readText()
            val extension = drawable.file.extension.orEmpty()

            return when {
                extension in rasterExtensions -> {
                    ImageIO.read(drawable.file.inputStream)
                        .getScaledInstance(size, size, Image.SCALE_SMOOTH)
                        .toBufferedImage()
                }

                canParseXmlContent(content) -> {
                    val svg = vectorToSvg(content, drawable.module.displayName, colors, dependencies)
                    svgToBufferedImage(svg)
                }

                else -> {
                    val rendered = rendererManager.getViewer(drawable.file)
                    rendered.getImage(drawable.file, drawable.module, Dimension(size, size)).get()
                }
            }
        } catch (e: Throwable) {
            return null
        }
    }

    private fun canParseXmlContent(content: String): Boolean {
        return content.startsWith("<vector") ||
                (content.startsWith("<?xml version=\"1.0\" encoding=\"utf-8\"?>") && content.contains("<vector"))
    }

    private fun svgToBufferedImage(svgString: String, size: Double = 50.0): BufferedImage? {
        val svgUniverse = SVGUniverse()
        val svgUri = svgUniverse.loadSVG(StringReader(svgString), "svg")
        val svgDiagram = svgUniverse.getDiagram(svgUri)

        val width = size.toInt()
        val height = size.toInt()

        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2d = bufferedImage.createGraphics()

        try {
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                java.awt.RenderingHints.VALUE_RENDER_QUALITY)

            svgDiagram.setIgnoringClipHeuristic(true)
            val scale = size / maxOf(svgDiagram.width, svgDiagram.height)
            val translateX = (size - svgDiagram.width * scale) / 2
            val translateY = (size - svgDiagram.height * scale) / 2

            g2d.translate(translateX, translateY)
            g2d.scale(scale, scale)
            svgDiagram.render(g2d)
        } finally {
            g2d.dispose()
        }

        return bufferedImage
    }
}