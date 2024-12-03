package com.puntogris.telescope.domain

import android.clip.cpp.CLIPAndroid
import com.intellij.openapi.vfs.VirtualFile
import com.puntogris.telescope.domain.usecase.FileToClip
import com.puntogris.telescope.utils.PLUGIN_NAME
import com.puntogris.telescope.utils.configPath
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

object Clip {

    private const val CLIP_VERBOSITY = 1
    private const val NUMB_THREADS = 4
    private const val VECTOR_DIMS = 512

    private var textClip: CLIPAndroid? = null
    private var visionClip: CLIPAndroid? = null

    private val fileToClip = FileToClip()

    private fun textClip(): CLIPAndroid? {
        if (textClip == null) {
            textClip = CLIPAndroid().apply {
                load(getTextModelPath(), CLIP_VERBOSITY)
            }
        }
        return textClip
    }

    private fun visionClip(): CLIPAndroid? {
        if (visionClip == null) {
            visionClip = CLIPAndroid().apply {
                load(getVisionModelPath(), CLIP_VERBOSITY)
            }
        }
        return visionClip
    }

    private fun getTextModelPath(): String? {
        val customModel = GlobalStorage.getTextModelPath()
        if (customModel.isNotEmpty()) {
            return customModel
        }
        val path = configPath.resolve(PLUGIN_NAME).resolve("CLIP-ViT-B-32-laion2B-s34B-b79K_ggml-model-f32.gguf")
        if (path.exists()) {
            return path.absolutePathString()
        }
        return null
    }

    private fun getVisionModelPath(): String? {
        val customModel = GlobalStorage.getVisionModelPath()
        if (customModel.isNotEmpty()) {
            return customModel
        }
        val path = configPath.resolve(PLUGIN_NAME).resolve("CLIP-ViT-B-32-laion2B-s34B-b79K_ggml-model-f32.gguf")
        if (path.exists()) {
            return path.absolutePathString()
        }
        return null
    }

    fun encodeFileImage(file: VirtualFile): Result<FloatArray> {
        return try {
            val converted = requireNotNull(
                fileToClip(file)
            )
            val emb = requireNotNull(visionClip()).encodeImage(
                converted.byteBuffer,
                converted.width,
                converted.height,
                NUMB_THREADS,
                VECTOR_DIMS,
                true
            )
            Result.success(emb)
        } catch (e: Throwable) {
            return Result.failure(e)
        }
    }

    fun encodeText(text: String): Result<FloatArray> {
        return try {
            val emb = requireNotNull(textClip()).encodeText(text, NUMB_THREADS, VECTOR_DIMS, true)
            Result.success(emb)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    fun canEnableClip(): Boolean {
        return getTextModelPath() != null && getVisionModelPath() != null
    }
}