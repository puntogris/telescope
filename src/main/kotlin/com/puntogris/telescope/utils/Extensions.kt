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
 * @return The converted BufferedImage
 */
fun Image.toBufferedImage(): BufferedImage {
    if (this is BufferedImage) {
        return this
    }

    // Create a buffered image with transparency
    val bimage = BufferedImage(getWidth(null), getHeight(null), BufferedImage.TYPE_INT_ARGB)

    // Draw the image on to the buffered image
    val bGr = bimage.createGraphics()
    bGr.drawImage(this, 0, 0, null)
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