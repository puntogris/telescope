package com.puntogris.telescope.ui.components

import com.android.tools.idea.ui.resourcemanager.plugin.DesignAssetRendererManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.ui.JBImageIcon
import com.puntogris.telescope.domain.DrawableCache
import com.puntogris.telescope.icons.MyIcons
import com.puntogris.telescope.models.DrawableRes
import com.puntogris.telescope.utils.toImage
import java.awt.Component
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import javax.swing.Icon

class IconProvider {
    private val imageIcon = JBImageIcon(MyIcons.NotSupportedIcon.toImage())
    private val fetchImageExecutor = service<FetchImageExecutor>()
    private val drawableCache = DrawableCache.createImageCache({})
    private val drawableRenderer = DrawableRenderer()

    fun getIcon(
        drawable: DrawableRes,
        component: Component,
        refreshCallback: () -> Unit,
        shouldBeRendered: () -> Boolean
    ): Icon {
        val image = drawableCache.computeAndGet(drawable.file, BufferedImage(300, 300, 1), false, refreshCallback) {
            if (shouldBeRendered()) {
                CompletableFuture.supplyAsync(
                    { if (shouldBeRendered()) drawableRenderer.render(drawable) else null },
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
}

class DrawableRenderer {
    private val rendererManager = DesignAssetRendererManager.getInstance()

    fun render(drawable: DrawableRes): BufferedImage? {
        try {
            val rendered = rendererManager.getViewer(drawable.file)
            return rendered.getImage(drawable.file, drawable.module, Dimension(50, 50)).get()
        } catch (e: Throwable) {
            return null
        }
    }
}