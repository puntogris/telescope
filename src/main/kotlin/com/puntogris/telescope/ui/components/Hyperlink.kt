package com.puntogris.telescope.ui.components

import java.awt.Color
import java.awt.Cursor
import java.awt.Desktop
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URI
import javax.swing.JLabel
import javax.swing.JOptionPane

class Hyperlink(url: String) : JLabel() {

    init {
        text = "<html><a href=''>$url</a></html>"
        foreground = Color.BLUE.darker()
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                try {
                    Desktop.getDesktop().browse(URI(url))
                } catch (ex: Exception) {
                    JOptionPane.showMessageDialog(
                        this@Hyperlink,
                        "Failed to open link: $url", "Error", JOptionPane.ERROR_MESSAGE
                    )
                }
            }

            override fun mouseEntered(e: MouseEvent) {
                text = "<html><a href='' style='text-decoration: underline;'>$url</a></html>"
            }

            override fun mouseExited(e: MouseEvent) {
                text = "<html><a href=''>$url</a></html>"
            }
        })
    }
}