package com.puntogris.telescope.models

import java.nio.ByteBuffer

class ImageResult(
    val name: String,
    val byteBuffer: ByteBuffer,
    val width: Int,
    val height: Int
)