package com.puntogris.telescope.domain

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readBytes
import com.intellij.openapi.vfs.readText
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.JPEGTranscoder
import org.apache.batik.transcoder.image.PNGTranscoder
import com.puntogris.telescope.models.ImageResult
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import javax.imageio.ImageIO
import javax.xml.parsers.DocumentBuilderFactory

object Convert {

    fun toClipCompatible(file: VirtualFile): ImageResult? {
        return when(file.extension) {
            "png" -> {
                val bufferedImage = ImageIO.read(ByteArrayInputStream(file.readBytes()))
                val buff = bufferedImageToByteBuffer(bufferedImage)
                ImageResult(
                    name = file.name,
                    byteBuffer = buff,
                    width = bufferedImage.width,
                    height = bufferedImage.height
                )
            }
            "xml" -> {
                val xml = file.readText()
                val svg = VectorDrawableConverter().transform(xml)
                val bufferedImage = convertSvgToRaster(svg, "png")
                val buff = bufferedImageToByteBuffer(bufferedImage)
                ImageResult(
                    name = file.name,
                    byteBuffer = buff,
                    width = bufferedImage.width,
                    height = bufferedImage.height
                )
            }
            else -> null
        }
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

    private fun convertSvgToRaster(svgContent: String, format: String): BufferedImage {
        val (width, height) = getSvgDimensions(svgContent)

        val inputStream = ByteArrayInputStream(svgContent.toByteArray())
        val transcoderInput = TranscoderInput(inputStream)
        val outputStream = ByteArrayOutputStream()
        val transcoderOutput = TranscoderOutput(outputStream)

        val transcoder = when (format.lowercase()) {
            "png" -> PNGTranscoder()
            "jpeg", "jpg" -> JPEGTranscoder().apply {
                addTranscodingHint(JPEGTranscoder.KEY_QUALITY, 0.8f)  // Set JPEG quality
            }

            else -> throw IllegalArgumentException("Unsupported format: $format")
        }

        transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width.toFloat())
        transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height.toFloat())
        transcoder.transcode(transcoderInput, transcoderOutput)

        inputStream.close()
        outputStream.flush()

        val byteBuffer = outputStream.toByteArray()
        outputStream.close()

        return ImageIO.read(ByteArrayInputStream(byteBuffer))
    }

    private fun getSvgDimensions(svgContent: String): Pair<Int, Int> {
        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val inputStream = ByteArrayInputStream(svgContent.toByteArray())
        val document = documentBuilder.parse(inputStream)
        val svgElement = document.documentElement

        val width = svgElement.getAttribute("width").toIntOrNull() ?: 300  // Default width if undefined
        val height = svgElement.getAttribute("height").toIntOrNull() ?: 300  // Default height if undefined

        inputStream.close()
        return Pair(width, height)
    }
}