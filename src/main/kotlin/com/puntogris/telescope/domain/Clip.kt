package com.puntogris.telescope.domain

import android.clip.cpp.CLIPAndroid
import com.puntogris.telescope.domain.usecase.FileToClip
import com.puntogris.telescope.domain.usecase.GetModelsPath
import com.puntogris.telescope.models.DrawableRes
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

object Clip {

    private const val CLIP_VERBOSITY = 1
    private const val NUMB_THREADS = 4
    private const val VECTOR_DIMS = 512

    private var textClip: CLIPAndroid? = null
    private var visionClip: CLIPAndroid? = null

    private val fileToClip = FileToClip()
    private val modelsPath = GetModelsPath()

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
        val path = Path(modelsPath().textModel)
        if (path.exists()) {
            return path.absolutePathString()
        }
        return null
    }

    private fun getVisionModelPath(): String? {
        val path = Path(modelsPath().visionModel)
        if (path.exists()) {
            return path.absolutePathString()
        }
        return null
    }

    fun encodeFileImage(res: DrawableRes): Result<FloatArray> {
        return try {
            val converted = requireNotNull(
                fileToClip(res)
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
            //TODO we should return the error mb, if it fails we should save the file in the db for partial match
            return Result.success(floatArrayOf())
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