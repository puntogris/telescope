package com.puntogris.telescope.utils

import com.android.tools.idea.rendering.DrawableRenderer
import com.intellij.openapi.module.Module
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.AppExecutorUtil
import org.jetbrains.android.facet.AndroidFacet
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

fun createRenderer(
    module: Module,
    targetFile: VirtualFile,
): CompletableFuture<DrawableRenderer> {
    val facet =
        AndroidFacet.getInstance(module)
            ?: return CompletableFuture<DrawableRenderer>().also {
                it.completeExceptionally(NullPointerException("Facet for module $module couldn't be found for use in DrawableRenderer."))
            }

    return CompletableFuture.supplyAsync(
        Supplier { return@Supplier DrawableRenderer(facet, targetFile) },
        AppExecutorUtil.getAppExecutorService()
    )
}