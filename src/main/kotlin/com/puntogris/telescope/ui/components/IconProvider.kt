package com.puntogris.telescope.ui.components

import com.android.tools.idea.ui.resourcemanager.plugin.DesignAssetRendererManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.util.Disposer
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.ui.JBImageIcon
import com.puntogris.telescope.storage.DrawableCache
import com.puntogris.telescope.icons.MyIcons
import com.puntogris.telescope.models.DrawableRes
import com.puntogris.telescope.utils.toImage
import java.awt.Component
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
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
        component: Component,
        refreshCallback: () -> Unit,
        shouldBeRendered: () -> Boolean
    ): Icon {
        val image = drawableCache.computeAndGet(drawable.file, placeholder, false, refreshCallback) {
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

    override fun dispose() {
        disposable.dispose()
    }
}

class DrawableRenderer {
    private val rendererManager = DesignAssetRendererManager.getInstance()

    fun render(drawable: DrawableRes, size: Int = DEFAULT_SIZE): BufferedImage? {
        try {
            val rendered = rendererManager.getViewer(drawable.file)
            return rendered.getImage(drawable.file, drawable.module, Dimension(size, size)).get()
        } catch (e: Throwable) {
            return null
        }
    }
}