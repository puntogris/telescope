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

fun Image.toBufferedImage(): BufferedImage {
    if (this is BufferedImage) {
        return this
    }
    val bimage = BufferedImage(getWidth(null), getHeight(null), BufferedImage.TYPE_INT_ARGB)

    val bGr = bimage.createGraphics()
    bGr.drawImage(this, 0, 0, null)
    bGr.dispose()

    return bimage
}