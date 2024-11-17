package com.puntogris.telescope.utils

import java.awt.Image
import java.awt.image.BufferedImage
import javax.swing.event.DocumentEvent

val DocumentEvent.documentText: String
    get() = try {
        document.getText(0, document.length)
    } catch (ignored: Throwable) {
        ""
    }

/**
 * Converts a given Image into a BufferedImage
 *
 * @param img The Image to be converted
 * @return The converted BufferedImage
 */
fun toBufferedImage(img: Image): BufferedImage {
    if (img is BufferedImage) {
        return img
    }

    // Create a buffered image with transparency
    val bimage = BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB)

    // Draw the image on to the buffered image
    val bGr = bimage.createGraphics()
    bGr.drawImage(img, 0, 0, null)
    bGr.dispose()

    // Return the buffered image
    return bimage
}


// TODO we should map res colors
fun String.replaceUnknownColors(): String {
    val fillRegex = """fill="[^"]*"""".toRegex()
    val strokeRegex = """stroke="[^"]*"""".toRegex()

    return replace(fillRegex, """fill="#000000"""").replace(strokeRegex, """stroke="#000000"""")
}
