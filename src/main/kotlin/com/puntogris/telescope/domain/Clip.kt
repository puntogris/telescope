package com.puntogris.telescope.domain

import android.clip.cpp.CLIPAndroid
import com.intellij.openapi.vfs.VirtualFile
import com.puntogris.telescope.domain.usecase.FileToClip

object Clip {

    private const val CLIP_VERBOSITY = 1
    private const val NUMB_THREADS = 4
    private const val VECTOR_DIMS = 512

    //TODO mb we should only load it once
    private val clip: CLIPAndroid
        get() = CLIPAndroid().apply {
            load("/Users/joaquin/Downloads/CLIP-ViT-B-32-laion2B-s34B-b79K_ggml-model-f32.gguf", CLIP_VERBOSITY)
        }

    private val fileToClip = FileToClip()

    fun encodeFileImage(file: VirtualFile): Result<FloatArray> {
        return try {
            val converted = requireNotNull(
                fileToClip(file)
            )
            val emb = clip.encodeImage(
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
            val emb = clip.encodeText(text, NUMB_THREADS, VECTOR_DIMS, true)
            Result.success(emb)
        } catch (e: Throwable) {
            Result.failure(e)
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