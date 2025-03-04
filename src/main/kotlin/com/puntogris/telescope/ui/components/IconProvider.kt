package com.puntogris.telescope.ui.components

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.util.Disposer
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.ui.JBImageIcon
import com.puntogris.telescope.storage.DrawableCache
import com.puntogris.telescope.icons.MyIcons
import com.puntogris.telescope.models.Colors
import com.puntogris.telescope.models.Dependencies
import com.puntogris.telescope.models.DrawableRes
import com.puntogris.telescope.utils.toImage
import java.awt.image.BufferedImage
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import javax.swing.Icon

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