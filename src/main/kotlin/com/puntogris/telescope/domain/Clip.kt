package com.puntogris.telescope.domain

import android.clip.cpp.CLIPAndroid
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.vfs.VirtualFile

object Clip {

    private const val CLIP_VERBOSITY = 1
    private const val NUMB_THREADS = 4
    private const val VECTOR_DIMS = 512

    //TODO mb we should only load it once
    private val clip: CLIPAndroid
        get() = CLIPAndroid().apply {
            load(GlobalStorage.getModelPath(), CLIP_VERBOSITY)
        }

    fun encodeFileImage(file: VirtualFile): FloatArray {
        val converted = Convert.toClipCompatible(file)
        return if (converted != null) {
            return clip.encodeImage(
                converted.byteBuffer,
                converted.width,
                converted.height,
                NUMB_THREADS,
                VECTOR_DIMS,
                true
            )
        } else {
            FloatArray(1)
        }
    }

    fun encodeText(text: String): Result<FloatArray> {
        try {
            val r = clip
            thisLogger().warn("logeto??okaa")
            return Result.success(floatArrayOf())
        } catch (e: Throwable) {
            thisLogger().warn("logeto??fail")

            return Result.failure(e)
        }
    }

    fun isValidModel(): Boolean {
        try {
            clip
            return true
        } catch (e: Throwable) {
            return false
        }
    }
}