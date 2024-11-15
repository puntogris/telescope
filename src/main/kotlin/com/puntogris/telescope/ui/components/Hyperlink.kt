package com.puntogris.telescope.ui.components

import java.awt.Color
import java.awt.Cursor
import java.awt.Desktop
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.net.URI
import javax.swing.JLabel
import javax.swing.JOptionPane

class Hyperlink(private val url: String) : JLabel(), MouseListener {

    init {
        text = "<html><a href=''>$url</a></html>"
        foreground = Color.BLUE.darker()
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        addMouseListener(this)
    }

    override fun mouseClicked(e: MouseEvent?) {
        try {
            Desktop.getDesktop().browse(URI(url))
        } catch (ex: Exception) {
            JOptionPane.showMessageDialog(
                this,
                "Failed to open link: $url",
                "Error",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    override fun mouseEntered(e: MouseEvent?) {
        text = "<html><a href='' style='text-decoration: underline;'>$url</a></html>"
    }

    override fun mouseExited(e: MouseEvent?) {
        text = "<html><a href=''>$url</a></html>"
    }

    override fun mousePressed(e: MouseEvent?) = Unit

    override fun mouseReleased(e: MouseEvent?) = Unit
}