package com.puntogris.telescope.storage
/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.android.tools.idea.ui.resourcemanager.model.Asset
import com.android.tools.idea.ui.resourcemanager.model.AssetKey
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.EdtExecutorService
import com.intellij.util.ui.update.MergingUpdateQueue
import org.jetbrains.annotations.Async
import java.awt.image.BufferedImage
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import kotlin.math.pow

private val CACHE_WEIGHT_BYTES = (100 * 1024.0.pow(2)).toLong() // 100 MB

private data class CachedImage(val image: BufferedImage, val modificationStamp: Long) {
    val size: Int
        get() = image.raster.dataBuffer.size * Integer.BYTES
}

/**
 * Helper class that caches the result of a computation of [BufferedImage].
 *
 * The keys of the cache are strong references to let
 * @see Cache
 * @see CacheBuilder.softValues
 */
class DrawableCache private constructor(
    mergingUpdateQueue: MergingUpdateQueue?,
    private val objectToImage: Cache<String, CachedImage>
) : Disposable {
    companion object {
        private val objectToImageCache by lazy {
            createObjectToImageCache(5, CACHE_WEIGHT_BYTES)
        }

        /**
         * Returns an ImageCache that uses an image pool of size [CACHE_WEIGHT_BYTES] to store previews for a given [Asset]
         *
         * @param parentDisposable Used to dispose of the returned [ImageCache], used as the parent disposable for the default
         * [MergingUpdateQueue] when the [mergingUpdateQueue] parameter is null.
         */
        fun createImageCache(
            parentDisposable: Disposable, mergingUpdateQueue: MergingUpdateQueue? = null
        ) = DrawableCache(mergingUpdateQueue, objectToImageCache).apply { Disposer.register(parentDisposable, this) }
    }

    private val pendingFutures = HashMap<VirtualFile, CompletableFuture<*>?>()

    private val updateQueue = mergingUpdateQueue ?: MergingUpdateQueue(
        "queue", 3000, true, MergingUpdateQueue.ANY_COMPONENT, this, null, false
    )

    @Async.Schedule
    private fun runOrQueue(
        asset: VirtualFile, executeImmediately: Boolean = false, runnable: () -> Unit
    ) {
        // We map to null to mark that the computation for asset has started and avoid any new computation.
        // It will then be replaced by the computation future once it is created.
        pendingFutures[asset] = null
        runnable()
        // TODO this would be cool but Update import is breaking things
//        if (executeImmediately) {
//            runnable()
//        } else {
//            val update = Update.create(asset.name, runnable)
//            updateQueue.queue(update)
//        }
    }

    override fun dispose() {
        synchronized(pendingFutures) {
            pendingFutures.values.forEach { it?.cancel(true) }
        }
    }

    fun clear(asset: Asset) {
        objectToImage.invalidate(asset.key)
    }

    fun clear() {
        objectToImage.invalidateAll()
    }

    /**
     * Return the value identified by [AssetKey] in the cache if it exists, otherwise returns the [placeholder] image
     * and gets the image from the [CompletableFuture] returned by [computationFutureProvider].
     *
     * If [forceComputation] is true, the [CompletableFuture] will be ran even if a value is present in the cache.
     *
     * Note that if a value is present in the cache and [forceComputation] is true, the returned [BufferedImage] will be the value from
     * the cache.
     *
     * Once the image is cached, [onImageCached] is invoked on [executor] (or the EDT if none is provided)
     */
    fun computeAndGet(
        @Async.Schedule asset: VirtualFile,
        placeholder: BufferedImage,
        forceComputation: Boolean,
        onImageCached: () -> Unit = {},
        executor: Executor = EdtExecutorService.getInstance(),
        computationFutureProvider: (() -> CompletableFuture<out BufferedImage?>)
    ): BufferedImage {
        val memoryCache = objectToImage.getIfPresent(asset.path)
        if (memoryCache != null) {
            return memoryCache.image
        }

        // we trigger disk cache for fast drawing, it should only run once, then hit the memory cache
        val diskCache = DiskCache.getIfPresent(asset.path)

        if (!pendingFutures.containsKey(asset)) {
            runOrQueue(asset, true) {
                startComputation(computationFutureProvider, asset, onImageCached, executor)
            }
        }
        return diskCache ?: placeholder
    }

    private fun startComputation(
        computationFutureProvider: () -> CompletableFuture<out BufferedImage?>,
        @Async.Execute asset: VirtualFile,
        onImageCached: () -> Unit,
        executor: Executor
    ) {
        val future = computationFutureProvider().thenAccept { image: BufferedImage? ->
            synchronized(pendingFutures) {
                pendingFutures.remove(asset)
            }
            if (image != null) {
                objectToImage.put(asset.path, CachedImage(image, 0))
                DiskCache.put(image, asset.path)
                executor.execute(onImageCached)
            } else {
                objectToImage.invalidate(asset.path)
            }
        }
        synchronized(pendingFutures) {
            if (!future.isDone) {
                pendingFutures[asset] = future
            }
        }
    }
}

private fun createObjectToImageCache(duration: Long, size: Long): Cache<String, CachedImage> =
    CacheBuilder.newBuilder()
        .expireAfterAccess(duration, TimeUnit.MINUTES)
        .softValues()
        .weigher<String, CachedImage> { _, image -> image.size }
        .maximumWeight(size)
        .build()