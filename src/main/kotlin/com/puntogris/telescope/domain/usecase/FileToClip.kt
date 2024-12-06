package com.puntogris.telescope.domain.usecase

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readBytes
import com.intellij.openapi.vfs.readText
import com.intellij.ui.JBColor
import com.puntogris.telescope.domain.DiskCache
import com.puntogris.telescope.models.ImageResult
import com.puntogris.telescope.utils.JPEG
import com.puntogris.telescope.utils.JPG
import com.puntogris.telescope.utils.PNG
import com.puntogris.telescope.utils.WEBP
import com.puntogris.telescope.utils.XML
import com.puntogris.telescope.utils.replaceUnknownColors
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.JPEGTranscoder
import org.apache.batik.transcoder.image.PNGTranscoder
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import javax.imageio.ImageIO
import javax.xml.parsers.DocumentBuilderFactory

class FileToClip {

    private val vectorToSvg = VectorToSvg()

    operator fun invoke(file: VirtualFile): ImageResult? {
        return when (file.extension) {
            PNG, WEBP -> {
                val bufferedImage = ImageIO.read(ByteArrayInputStream(file.readBytes()))
                val buff = bufferedImageToByteBuffer(bufferedImage)
                ImageResult(
                    name = file.name,
                    byteBuffer = buff,
                    width = bufferedImage.width,
                    height = bufferedImage.height
                )
            }

            XML -> {
                val cache = DiskCache.getIfPresent(file.path)
                if (cache != null) {
                    val buff = bufferedImageToByteBuffer(cache)
                    return ImageResult(
                        name = file.name,
                        byteBuffer = buff,
                        width = cache.width,
                        height = cache.height
                    )
                }
                val xml = file.readText()
                if (!xml.startsWith("<vector")) {
                    return null
                }

                val svg = vectorToSvg(xml).replaceUnknownColors()
                val bufferedImage = convertSvgToRaster(svg, PNG)
                DiskCache.put(bufferedImage, PNG, file.path)

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
            PNG -> PNGTranscoder()
            JPEG, JPG -> JPEGTranscoder().apply {
                addTranscodingHint(JPEGTranscoder.KEY_QUALITY, 0.8f)  // Set JPEG quality
            }

            else -> throw IllegalArgumentException("Unsupported format: $format")
        }

        transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width.toFloat() * 10 )
        transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height.toFloat() * 10)
        transcoder.transcode(transcoderInput, transcoderOutput)

        inputStream.close()
        outputStream.flush()

        val byteBuffer = outputStream.toByteArray()
        outputStream.close()

        return addWhiteBackground(ImageIO.read(ByteArrayInputStream(byteBuffer)))
    }

    private fun addWhiteBackground(originalImage: BufferedImage): BufferedImage {
        val resultImage = BufferedImage(
            originalImage.width,
            originalImage.height,
            BufferedImage.TYPE_INT_RGB
        )

        // Paint white background
        val graphics = resultImage.createGraphics()
        graphics.color = JBColor.WHITE
        graphics.fillRect(0, 0, resultImage.width, resultImage.height)

        // Draw original image on top
        graphics.drawImage(originalImage, 0, 0, null)
        graphics.dispose()

        return resultImage
    }

    private fun getSvgDimensions(svgContent: String): Pair<Int, Int> {
        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val inputStream = ByteArrayInputStream(svgContent.toByteArray())
        val document = documentBuilder.parse(inputStream)
        val svgElement = document.documentElement

        val width = svgElement.getAttribute("width").toIntOrNull() ?: 224  // Default width if undefined
        val height = svgElement.getAttribute("height").toIntOrNull() ?: 244  // Default height if undefined

        inputStream.close()
        return Pair(width, height)
    }
}