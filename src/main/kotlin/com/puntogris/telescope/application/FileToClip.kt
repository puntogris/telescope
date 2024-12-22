package com.puntogris.telescope.application

import com.puntogris.telescope.models.DrawableRes
import com.puntogris.telescope.models.ImageResult
import com.puntogris.telescope.ui.components.DrawableRenderer
import java.awt.Color
import java.awt.image.BufferedImage
import java.nio.ByteBuffer

private const val IMAGE_SIZE = 224

// TODO we loss white images when we only use white as bg
// we could convert it to gray scale but we lose the ability to search by color
// we could check image colors and then apply a appropriate bg color
class FileToClip {

    private val drawableRenderer = DrawableRenderer()

    operator fun invoke(drawable: DrawableRes): ImageResult? {
        val image = drawableRenderer.render(drawable, IMAGE_SIZE) ?: return null
        val imageWhiteBg = addWhiteBackground(image)
        val byteBuffer = bufferedImageToByteBuffer(imageWhiteBg)

        return ImageResult(
            name = drawable.name,
            byteBuffer = byteBuffer,
            width = image.width,
            height = image.height
        )
    }

    private fun bufferedImageToByteBuffer(image: BufferedImage): ByteBuffer {
        val width = image.width
        val height = image.height
        val imageBuffer = ByteBuffer.allocateDirect(width * height * 3)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = image.getRGB(x, y)
                imageBuffer.put((pixel shr 16 and 0xFF).toByte())  // Red channel
                imageBuffer.put((pixel shr 8 and 0xFF).toByte())   // Green channel
                imageBuffer.put((pixel and 0xFF).toByte())         // Blue channel
            }
        }
        return imageBuffer
    }

    /**
     * Using BufferedImage directly instead of UIUtil or ImageUtil to avoid interference with embedding creation.
     * Also, using Color instead of JBColor, as the latter doesn't provide true white in this context.
     */
    private fun addWhiteBackground(originalImage: BufferedImage): BufferedImage {
        val resultImage = BufferedImage(
            originalImage.width,
            originalImage.height,
            BufferedImage.TYPE_INT_RGB
        )

        // Paint white background
        val graphics = resultImage.createGraphics()
        graphics.color = Color.white
        graphics.fillRect(0, 0, resultImage.width, resultImage.height)

        // Draw original image on top
        graphics.drawImage(originalImage, 0, 0, null)
        graphics.dispose()

        return resultImage
    }
}