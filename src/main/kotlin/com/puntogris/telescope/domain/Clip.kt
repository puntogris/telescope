package com.puntogris.telescope.domain

import android.clip.cpp.CLIPAndroid
import com.intellij.openapi.vfs.VirtualFile

object Clip {

    private const val CLIP_MODEL_PATH = "/Users/joaquin/Documents/CLIP-ViT-B-32-laion2B-s34B-b79K_ggml-model-f16.gguf"
    private const val CLIP_VERBOSITY = 1
    private const val NUMB_THREADS = 4
    private const val VECTOR_DIMS = 512

    //TODO mb we should only load it once
    private val clip: CLIPAndroid
        get() = CLIPAndroid().apply { load(CLIP_MODEL_PATH, CLIP_VERBOSITY) }

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

    fun encodeText(text: String) :FloatArray {
       return  clip.encodeText(text, NUMB_THREADS, VECTOR_DIMS, true)
    }
}